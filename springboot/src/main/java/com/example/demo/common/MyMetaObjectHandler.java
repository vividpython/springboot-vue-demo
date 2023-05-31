package com.example.demo.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
// 自动填充配置
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime",new Date(),metaObject); // 添加时填充createtime字段为当前的时间
        this.setFieldValByName("updateTime",new Date(),metaObject); // 添加时填充updatetime字段为当前的时间
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime",new Date(),metaObject); // 更新时填充updatetime字段为当前的时间
    }
}

