package com.ebay.signaturevalidation.controller;

import com.ebay.signaturevalidation.service.UpdateDataService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UpdataDataController {

    @Autowired
    private UpdateDataService updateDataService;

    @GetMapping("/findAll")
    private void findAll(){
        updateDataService.updateData();
    }

}
