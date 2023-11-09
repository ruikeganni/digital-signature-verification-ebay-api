package com.ebay.signaturevalidation.vo;

import lombok.Data;

@Data
public class ReturnResponse {

    private String signature;

    private String signature_input;

    public ReturnResponse(String signature,String signatureInput){
        this.signature = signature;
        this.signature_input = signatureInput;
    }

}
