package com.ebay.signaturevalidation.job;

import com.ebay.signaturevalidation.service.StoreService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlatformGetOrderJob {

    @Autowired
    private StoreService storeService;

    @XxlJob("getOrder")
    private void getOrderJob () {
        try {

        } catch (Exception e) {
            log.info("error", e.getMessage());
        }
    }

}
