package com.ebay.signaturevalidation.main;

import com.alibaba.fastjson.JSON;
import com.ebay.signaturevalidation.service.impl.RequestServiceImpl;
import com.ebay.signaturevalidation.service.impl.RestTemplateImpl;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONUtil;
import com.nimbusds.jose.util.JSONArrayUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestMain {

    static String getDataUrl = "https://7587609.extforms.netsuite.com/app/site/hosting/scriptlet.nl?script=545&deploy=1&compid=7587609&h=6f2bfbbec506d7d44cb9";

    public static void main(String[] args) {
        ResponseEntity<String> getDataResponse = RestTemplateImpl.getRestTemplate().exchange(getDataUrl, HttpMethod.GET, null, String.class);
        List<Map<String, String>> mapList = (List<Map<String, String>>) JSON.parseObject(getDataResponse.getBody(), Object.class);
        mapList.stream().forEach(o -> {
            o.put("operate", "DEL");
        });
        RequestServiceImpl requestService = new RequestServiceImpl();
        requestService.request(mapList);
    }

}