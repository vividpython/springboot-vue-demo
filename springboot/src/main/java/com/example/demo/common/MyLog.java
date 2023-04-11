package com.example.demo.common;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyLog {
    // 自定义模块名，eg:登录
    String title() default "";
    // 方法传入的参数

    // 操作类型，eg:INSERT, UPDATE...
    BusinessType businessType() default BusinessType.OTHER;

    String operParam() default "";
}
