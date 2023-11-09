package com.ebay.signaturevalidation.service.impl;

import com.ebay.signaturevalidation.mapper.UpdateDataMapper;
import com.ebay.signaturevalidation.po.UpdateData;
import com.ebay.signaturevalidation.service.UpdateDataService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MapperScan("com.ebay.signaturevalidation.mapper")
@Service
public class UpdateDataServiceImpl implements UpdateDataService {

    @Autowired
    private UpdateDataMapper updateDataMapper;

    static Map<String, String> resultMap(UpdateData updateData) {
        Map<String, String> map = new HashMap<>();
        map.put("id", updateData.getRecId() + "");
        map.put("type", updateData.getType()); // itemfulfillment
        map.put("value", updateData.getValue());
        map.put("operate", "UPDATE");
        return map;
    }

    @Override
    public void updateData() {
        try{
            List<UpdateData> list = updateDataMapper.selectList(null);
            List<Map<String, String>> mapList = new ArrayList<>();
            for(UpdateData data: list) {
                mapList.add(resultMap(data));
            }
            RequestServiceImpl requestService = new RequestServiceImpl();
            requestService.request(mapList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
