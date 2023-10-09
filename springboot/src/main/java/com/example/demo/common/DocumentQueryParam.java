package com.example.demo.common;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

public class DocumentQueryParam {

    //项目编号
    private String itemNo;

    // 项目料号
    private String materialNo;
    //文件类型
    private List<Integer> documentType;

    public Integer getDepartId() {
        return departId;
    }

    public void setDepartId(Integer departId) {
        this.departId = departId;
    }

    //文件所属事业部的id
    private Integer departId;


    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    //审核状态
    private Integer approvalStatus;


    //发布状态 0表示未发布 1表示已发布
    private Integer publishStatus;  // 发布状态


    //部门id 根据创建者的部门来确定 表示该文件的归属的设计部门是哪个

    //创建人
    private Integer userId;

    //排序列
    private String sortby;
    //排序方式
    private String order;


    //创建时间开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTimeStart;

    //创建时间结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTimeEnd;

    //发布时间开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date publishTimeStart;

    public Date getPublishTimeStart() {
        return publishTimeStart;
    }

    public void setPublishTimeStart(Date publishTimeStart) {
        this.publishTimeStart = publishTimeStart;
    }

    public Date getPublishTimeEnd() {
        return publishTimeEnd;
    }

    public void setPublishTimeEnd(Date publishTimeEnd) {
        this.publishTimeEnd = publishTimeEnd;
    }

    //发布时间结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date publishTimeEnd;



    public String getSortby() {
        return sortby;
    }

    public void setSortby(String sortby) {
        this.sortby = sortby;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }



    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }


    public Date getCreateTimeStart() {
        return createTimeStart;
    }
    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }



    public String getMaterialNo() {
        return materialNo;
    }

    public void setMaterialNo(String materialNo) {
        this.materialNo = materialNo;
    }




    public void setCreateTimeStart(Date createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public Date getCreateTimeEnd() {
        return createTimeEnd;
    }




    public void setCreateTimeEnd(Date createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public List<Integer> getDocumentType() {
        return documentType;
    }



    public void setDocumentType(List<Integer> documentType) {
        this.documentType = documentType;
    }




}
