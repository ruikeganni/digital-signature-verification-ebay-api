package com.ebay.signaturevalidation.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Date;

@Data
@TableName(value = "t_request_param")
public class RequestParam {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer storeId;

    private Date startDate;

    private Date endDate;

    private Integer pageSize;

    private String pageNext;

}
