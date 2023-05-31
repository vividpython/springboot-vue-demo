package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@TableName("user")
@Data
public class User {
    @TableId(type = IdType.AUTO,value = "id")
    private Integer id;
    private String username;
    private String password;
    private String nickName;

    private String img;

    private Integer role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    //@Version
    private Integer version;
    //@TableLogic
    private Integer deleted;

    private Integer departId;

    @TableField(exist = false)  // 此字段不更新
    private Depart depart;    // 员工部门
}
