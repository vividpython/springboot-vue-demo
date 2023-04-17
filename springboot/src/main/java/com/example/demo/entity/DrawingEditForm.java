package com.example.demo.entity;

import com.example.demo.entity.Drawing;
import com.example.demo.entity.EditDrawing;
import lombok.Data;

@Data
public class DrawingEditForm {
    private Drawing drawing;
    private EditDrawing editDrawing;
}