package com.example.demo.common;

public class DocumentQueryParam {
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

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    //项目编号
    private String itemNo;
    // 项目料号
    private String materialNo;
    //文件类型
    private Integer fileType;

}
