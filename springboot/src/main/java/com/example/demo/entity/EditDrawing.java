package com.example.demo.entity;


import lombok.Data;

import java.io.Serializable;


@Data
public class EditDrawing implements Serializable {

    private String drawingName;
    private Integer drawingType;

    public EditDrawing() {
    }

    public EditDrawing(String drawingName, Integer drawingType) {
        this.drawingName = drawingName;
        this.drawingType = drawingType;
    }
}
