package com.example.demo.entity;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class TypeDistributionData implements Serializable {

    private String name;
    private Integer value;

    public TypeDistributionData( String name, Integer value) {
        this.name = name;
        this.value = value;
    }
}
