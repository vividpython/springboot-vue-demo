package com.example.demo.common;

public class DrawingQueryParam {
    // 料号
    private String productNo;
    //项目号
    private String itemNo;

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    // 图纸名称
    private String drawingName;
    //图纸类型
    private Integer drawingType;

    public Integer getDrawingType() {
        return drawingType;
    }

    public void setDrawingType(Integer drawingType) {
        this.drawingType = drawingType;
    }

    // 图纸存储路径
    private String drawingPath;

    public String getProductNo() {
        return productNo;
    }

    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }

    public String getDrawingName() {
        return drawingName;
    }

    public void setDrawingName(String drawingName) {
        this.drawingName = drawingName;
    }

    public String getDrawingPath() {
        return drawingPath;
    }

    public void setDrawingPath(String drawingPath) {
        this.drawingPath = drawingPath;
    }
}
