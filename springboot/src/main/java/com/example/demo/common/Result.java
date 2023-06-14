package com.example.demo.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Result<T> implements Serializable {


    public String code;

    private String msg;

    private T data;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Result() {
    }

    public Result(T data) {
        this.data = data;
    }
    public static Result success(){
        Result result = new Result<>();
        result.setMsg("成功");
        result.setCode("0");
        return result;
    }
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<T>(data);
        result.setMsg("成功");
        result.setCode("0");
        return result;
    }
    public static <T> Result<T> success(String code, String message,T data){
        Result<T> result = new Result<T>(data);
        result.setMsg(message);
        result.setCode(code);
        return result;
    }
    public static Result error(String code,String msg){
        Result result = new Result();
        result.setMsg(msg);
        result.setCode(code);
        return result;
    }

}