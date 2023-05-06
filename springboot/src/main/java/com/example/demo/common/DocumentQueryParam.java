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



    //项目编号
    private String itemNo;

    public Integer getDocumentType() {
        return documentType;
    }

    public void setDocumentType(Integer documentType) {
        this.documentType = documentType;
    }

    // 项目料号
    private String materialNo;
    //文件类型
    private Integer documentType;

}
