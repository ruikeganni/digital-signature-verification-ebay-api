package com.ebay.signaturevalidation.service.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class RequestServiceImpl {

    static String delUrl = "https://7587609.extforms.netsuite.com/app/site/hosting/scriptlet.nl?script=521&deploy=1&compid=7587609&h=3b8c77809151776a8208";

    public void request(List<Map<String, String>> mapList) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(20,
                25,
                10,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>());
        try{
            for (int i = 0; i < mapList.size(); i++) {
                int finalI = i;
                List<Map<String, String>> finalMapList = mapList;
                Thread thread = new Thread(() -> {
                    System.out.println(Thread.currentThread());
                    HttpEntity httpEntity = new HttpEntity<>(finalMapList.get(finalI), RestTemplateImpl.getHttpHeaders());
                    ResponseEntity<String> responseEntity = RestTemplateImpl.getRestTemplate().exchange(delUrl, HttpMethod.POST, httpEntity, String.class);
                    System.out.println(responseEntity.getBody());
                });
                executor.execute(thread);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            executor.shutdown();
        }
        while (true) {
            if(executor.isTerminated()){
                System.out.println("end...");
                break;
            }
        }
    }

}
