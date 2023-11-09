package com.ebay.signaturevalidation.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "update_data")
public class UpdateData {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer recId;

    private String type;

    private String field;

    private String value;

    private String state;

    private String message;

}
