package com.ebay.signaturevalidation.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "t_store")
public class Store {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String storeName;

    private String platform;

    private String clientId;

    private String clientSecret;

    private String isinactive;

}
