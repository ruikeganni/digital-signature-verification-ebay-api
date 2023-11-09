package com.ebay.signaturevalidation.service;


import com.ebay.signaturevalidation.SignatureException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.spec.EdDSAParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;


@Service
public class KeypairService {
    private final JWEEncrypter jweEncrypter;
    private final JWEDecrypter jweDecrypter;

//    private static final String KEYS_FOLDER = "D:/test/ebay-signatures-jdk/digital-signature-verification-ebay-api/src/main/resources/keys/";

//    private static final String KEYS_FOLDER = "/root/";

    private static final String KEYS_FOLDER = "src/main/resources/keys/";

    private final String algorithm = EdDSAParameterSpec.Ed25519;

    public KeypairService() throws com.ebay.signaturevalidation.SignatureException {
        try {
//            Path masterkeyPath = Path.of(KEYS_FOLDER + "masterkey.key");
//            String secretKeyBase64 = Files.readString(masterkeyPath);
            String secretKeyBase64 = "uSA8lSm4fWRrDs8JvjS/Y6D5Ohp3mIKC4AhN/nBaGxs=";
            final byte[] secretKey = Base64.decode(secretKeyBase64);

            jweEncrypter = new AESEncrypter(secretKey);
            jweDecrypter = new AESDecrypter(secretKey);
        } catch (KeyLengthException ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error loading master key: " + ex.getMessage(), ex);
        }
    }

    public EncryptedJWT decryptJWE(String jweString) throws com.ebay.signaturevalidation.SignatureException {
        try {
            EncryptedJWT jwe = EncryptedJWT.parse(jweString);
            jwe.decrypt(jweDecrypter);
            return jwe;
        } catch (ParseException | JOSEException ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error decrypting the JWE from x-ebay-signature-key header. Please note that you can only use the test keys and JWEs listed in the README, not one retrieved from the Key Management API. These will only work on the ebay APIs.", ex);
        }
    }

    public KeyPair loadExistingKeyPair() throws com.ebay.signaturevalidation.SignatureException {
        String algoFolder = algorithm.toLowerCase();
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream("settings/settings.ini");
        PrivateKey privateKey = readPrivateKey(KEYS_FOLDER + algoFolder + "/privatekey.pem");
        PublicKey publicKey = readPublicKey(KEYS_FOLDER + algoFolder + "/publickey.pem");
        return new KeyPair(publicKey, privateKey);
    }


    public String getJWE(PublicKey publicKey) throws com.ebay.signaturevalidation.SignatureException {
        try {
            // Compose the JWT claims set
            Date now = new Date();

            JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                    .expirationTime(new Date(now.getTime() + 1000L * 60 * 60 * 24 * 365 * 10)) // expires in 3 years
                    .notBeforeTime(now)
                    .issueTime(now)
                    .jwtID(UUID.randomUUID().toString())
                    .claim("appid", "app1") // this is set to the appId
                    .claim("pkey", new String(Base64.encode(publicKey.getEncoded()))) // public ed25519 key
                    .claim("cipher", algorithm) // this is set to the appId
                    .build();

//            logger.info("Claims: {}", jwtClaims.toJSONObject());

            // Request JWT encrypted with DIR and 256-bit AES/GCM
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.A256GCMKW, EncryptionMethod.A256GCM)
                    .compressionAlgorithm(CompressionAlgorithm.DEF)
                    .build();


            // Create the encrypted JWT object
            EncryptedJWT jwt = new EncryptedJWT(header, jwtClaims);

            // Do the actual encryption
            jwt.encrypt(jweEncrypter);

            // Serialise to JWT compact form
            String jwtString = jwt.serialize();
//            logger.info("JWT length: {}; content: {}", jwtString.length(), jwtString);

            return jwtString;
        } catch (JOSEException ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error creating JWE: " + ex.getMessage(), ex);
        }

    }

    private PublicKey readPublicKey(String file) throws com.ebay.signaturevalidation.SignatureException {
        try (FileReader keyReader = new FileReader(file)) {
            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemParser.readObject());
            return converter.getPublicKey(publicKeyInfo);
        } catch (IOException ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error loading public key: " + ex.getMessage(), ex);
        }
    }

    public PrivateKey readPrivateKey(String file) throws com.ebay.signaturevalidation.SignatureException {
        try (FileReader keyReader = new FileReader(file)) {

            PEMParser pemParser = new PEMParser(keyReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());

            return converter.getPrivateKey(privateKeyInfo);
        } catch (IOException ex) {
            throw new SignatureException("Error loading private key: " + ex.getMessage(), ex);
        }
    }

    public String getAlgorithm() {
        return algorithm;
    }

    @PostConstruct
    private void postConstruct() {
        Security.addProvider(new BouncyCastleProvider());
    }
}
