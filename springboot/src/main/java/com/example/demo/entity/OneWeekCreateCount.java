package com.example.demo.entity;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class OneWeekCreateCount implements Serializable {

    private Date date;
    private Integer value;

    public OneWeekCreateCount(Date date, Integer value) {
        this.date = date;
        this.value = value;
    }
}
