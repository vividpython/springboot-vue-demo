package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@TableName("document")
@Data
public class Document {
    @TableId(type = IdType.AUTO,value = "id")
    private Integer id;
    private String itemNo;
    private String materialNo;
    private String documentName;
    private Integer documentType;
    private Integer sequenceNo;


    private String documentVersion;
    private String documentPath;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    //创建人id
    private Integer userId;

    //创建人
    @TableField(exist = false)  // 此字段不更新
    private User user;    // 创建人信息

}
