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
    //审批状态 0表示未审批 1表示审批通过
    private Integer approvalStatus;  // 审批状态


    //发布状态 0表示未发布 1表示已发布
    private Integer publishStatus;  // 发布状态

    private String documentVersion;
    private String documentPath;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date approvalTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date publishTime;

    //创建人id
    private Integer userId;

    //变更人id
    private Integer subId;


    //部门id 即文件所属的事业部 由创建人创建时所在的部门决定
    private Integer departId;



    //默认值为0 表示启用 当值为1时表示弃用/作废
    private Integer deleted;  // 逻辑删除

    //备注字段  显示文件的源文件名称
    private String comment;


    //创建人
    @TableField(exist = false)  // 此字段不更新
    private User user;    // 创建人信息

    @TableField(exist = false)  // 此字段不更新
    private User substitution;    // 创建人信息

}
