package com.ebay.signaturevalidation.service.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

public class RestTemplateImpl {

    private static RestTemplate restTemplate = null;
    private static HttpHeaders headers = null;

    private RestTemplateImpl(){
    }

    public static RestTemplate getRestTemplate(){
        if(restTemplate == null) {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    public static HttpHeaders getHttpHeaders(){
        if(headers == null) {
            headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("User-Agent", "Mozilla/5.0");
        }
        return headers;
    }

}
