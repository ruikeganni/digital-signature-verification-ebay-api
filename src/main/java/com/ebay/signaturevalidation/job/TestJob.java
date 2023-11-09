package com.ebay.signaturevalidation.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class TestJob {

    @XxlJob("testJob")
    public void testJob(){
        System.out.println("111");
    }

}
