package com.ebay.signaturevalidation.controller;

import com.ebay.signaturevalidation.SignatureException;
import com.ebay.signaturevalidation.service.SignatureService;
import com.ebay.signaturevalidation.vo.ReturnResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private SignatureService signatureService;

    private URI uri = URI.create("https://apiz.ebay.com/sell/finances/v1/transaction");

    @GetMapping("/getSignature")
    public ReturnResponse getSignature() throws SignatureException {
        HttpRequest httpRequest = new MockClientHttpRequest(HttpMethod.GET, uri);
        return signatureService.getSignature(httpRequest, null);
    }

}
