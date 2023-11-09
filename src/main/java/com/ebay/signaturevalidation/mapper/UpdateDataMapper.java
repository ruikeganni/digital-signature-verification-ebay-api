package com.ebay.signaturevalidation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.signaturevalidation.po.UpdateData;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UpdateDataMapper extends BaseMapper<UpdateData> {

    @Select("select * from ")
    public List<UpdateData> findAll();

}
