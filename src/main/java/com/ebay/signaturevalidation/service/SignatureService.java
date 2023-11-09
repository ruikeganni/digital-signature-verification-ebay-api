package com.ebay.signaturevalidation.service;

import com.ebay.signaturevalidation.SignatureException;
import com.ebay.signaturevalidation.vo.ReturnResponse;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.List;


@Service
public class SignatureService {

    private final KeypairService keypairService;

    private final Logger logger = LoggerFactory.getLogger(SignatureService.class.getName());
//    private List<String> signatureParams = List.of("content-digest", "x-ebay-signature-key", "@method", "@path", "@authority");
    private List<String> signatureParams = List.of("x-ebay-signature-key", "@method", "@path", "@authority");
    private String signatureInput;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private String jwt;

    private String jwe = "eyJ6aXAiOiJERUYiLCJraWQiOiJiNmI4ZWY2MC0zODU4LTRiMGUtYTI5My1mZjQyOGJkZmMyZmMiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiNXFFekNibjFTaFYxMUpTS0ZXR1dsZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiSHUxVkpmdnBhWmp4V09VdyJ9.UvMuBe6GRNg7BNpYJkbNxo-EITKIOmpDJMcOiv5hBM0.uxWhH1krxU7bbKAt.VrRwrcxsCyW0-0bQL6N7P-7lfJRb7FmfLkuYBe6I6MvBEivMaNO302jLzZdjWrdl03FUNndEKiVMr4WSn5rtyMxmpYJEcAw63vADfulYgUomhURZJmot3Z0YPxwPilNzNnIC0oUrR6Ivm8h4HikJDUTpD8ggHPmRApoBSas8-kUeUfoFyF2-a11Gl-guyJFACqMgN592a4t79XiRoqTKltL36T6lBbddVDb5hc4mQUKa4OeILbCp9SqmD_R-dA.CXzHpRD14iSFdKDW2EH5MA";


    public SignatureService(KeypairService keypairService){
        this.keypairService = keypairService;
    }

    public void signMessage(HttpRequest request, byte[] body) throws com.ebay.signaturevalidation.SignatureException {
        if (body != null && body.length > 0) {
            addDigestHeader(request, body);
        } else {
            signatureParams = List.of("x-ebay-signature-key", "@method", "@path", "@authority");
        }
        addSignatureKeyHeader(request, jwe);
        addSignatureHeaders(request);

        logger.info("Message signed");
    }

    private void addSignatureHeaders(HttpRequest request) throws com.ebay.signaturevalidation.SignatureException {
        String signature = getSignatureValue(request);

        try {
            HttpHeaders headers = request.getHeaders();
            headers.add("Signature", "sig1=:" + signature + ":");
            headers.add("Signature-Input", "sig1=" + signatureInput);
            logger.info("signature: {}", headers.get("Signature"));
        } catch (Exception ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error adding Signature and Signature-Input headers: " + ex.getMessage(), ex);
        }
    }

    public ReturnResponse getSignature(HttpRequest request,byte[] body) throws com.ebay.signaturevalidation.SignatureException {
        if (body != null && body.length > 0) {
            addDigestHeader(request, body);
        } else {
            signatureParams = List.of("x-ebay-signature-key", "@method", "@path", "@authority");
        }
        addSignatureKeyHeader(request, jwe);
        String signature = getSignatureValue(request);
        try {
            HttpHeaders headers = request.getHeaders();
            headers.add("Signature", "sig1=:" + signature + ":");
            headers.add("Signature-Input", "sig1=" + signatureInput);
            logger.info("signature: {}", headers.get("Signature"));
        } catch (Exception ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error adding Signature and Signature-Input headers: " + ex.getMessage(), ex);
        }
        return new ReturnResponse("sig1=:" + signature + ":", "sig1=" + signatureInput);
    }

    private void addDigestHeader(HttpRequest request, byte[] body) throws com.ebay.signaturevalidation.SignatureException {
        try {
            HttpHeaders headers = request.getHeaders();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String digestString = "sha-256=:" + new String(Base64.encode(digest.digest(body))) + ":";

            headers.put("Content-digest", List.of(digestString));
        } catch (Exception ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error adding Content-Digest header: " + ex.getMessage(), ex);
        }
    }

    private void addSignatureKeyHeader(HttpRequest request, String jwt) throws com.ebay.signaturevalidation.SignatureException {
        try {
            HttpHeaders headers = request.getHeaders();
            headers.put("x-ebay-signature-key", List.of(jwt));
        } catch (Exception ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error adding x-ebay-signature-key header: " + ex.getMessage(), ex);
        }
    }

    public String getSignatureValue(HttpRequest request) throws com.ebay.signaturevalidation.SignatureException {
        try {
            String baseString = calculateBase(request);
            byte[] base = baseString.getBytes(StandardCharsets.UTF_8);

            Signer signer;
            if (keypairService.getAlgorithm().equals("RSA")) {
                signer = new RSADigestSigner(new SHA256Digest());
            } else {
                signer = new Ed25519Signer();
            }
            byte[] privateKeyByte = privateKey.getEncoded();
            AsymmetricKeyParameter privateKeyParameters = PrivateKeyFactory.createKey(privateKeyByte);
            signer.init(true, privateKeyParameters);
            signer.update(base, 0, base.length);
            byte[] signature = signer.generateSignature();

            return new String(Base64.encode(signature));
        } catch (CryptoException | IOException ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error creating value for signature: " + ex.getMessage(), ex);
        }
    }

    private String calculateBase(HttpRequest request) throws com.ebay.signaturevalidation.SignatureException {
        try {
            postConstruct();
            StringBuilder buf = new StringBuilder();
            HttpHeaders headers = request.getHeaders();

            for (String header : signatureParams) {
                buf.append("\"");
                buf.append(header.toLowerCase());
                buf.append("\": ");

                if (header.startsWith("@")) {
                    switch (header.toLowerCase()) {
                        case "@method":
                            buf.append(request.getMethod());
                            break;
                        case "@authority":
                            buf.append(request.getURI().getAuthority());
                            break;
                        case "@target-uri":
                            buf.append(request.getURI());
                            break;
                        case "@scheme":
                            buf.append(request.getURI().getScheme());
                            break;
//                    case "@request-target":
//                        // TBD
//                        break;
                        case "@path":
                            buf.append(request.getURI().getPath());
                            break;
                        case "@query":
                            buf.append(request.getURI().getQuery());
                            break;
//                    case "@query-param":
//                        // TBD
//                        break;
//                    case "@status":
//                        // TBD
//                        break;
                        default:
                            throw new com.ebay.signaturevalidation.SignatureException("Unknown pseudo header " + header);
                    }
                } else {
                    if (!headers.containsKey(header)) {
                        throw new com.ebay.signaturevalidation.SignatureException("Header " + header + " not included in message");
                    }

                    buf.append(headers.get(header).get(0));
                }

                buf.append("\n");
            }

            buf.append("\"@signature-params\": ");

            signatureInput = "";
            StringBuilder signatureInputBuf = new StringBuilder();
            signatureInputBuf.append("(");

            for (int i = 0; i < signatureParams.size(); i++) {
                String param = signatureParams.get(i);
                signatureInputBuf.append("\"");
                signatureInputBuf.append(param);
                signatureInputBuf.append("\"");
                if (i < signatureParams.size() - 1) {
                    signatureInputBuf.append(" ");
                }
            }

            signatureInputBuf.append(");created=");
//            signatureInputBuf.append("1681539853"); // TODO: This makes testing easier. But for real production code, you will want to use the next line instead of this one
            signatureInputBuf.append(Instant.now().getEpochSecond());
            signatureInput = signatureInputBuf.toString();

            buf.append(signatureInput);

            return buf.toString();
        } catch (Exception ex) {
            throw new com.ebay.signaturevalidation.SignatureException("Error calculating signature base: " + ex.getMessage(), ex);
        }
    }

    @PostConstruct
    private void postConstruct() throws SignatureException {
        KeyPair keyPair = keypairService.loadExistingKeyPair();
        this.privateKey = keyPair.getPrivate();
//        this.publicKey = keyPair.getPublic();
//        this.jwt = keypairService.getJWE(publicKey);
    }
}
