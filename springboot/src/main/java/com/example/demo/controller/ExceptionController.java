package com.example.demo.controller;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.demo.common.Result;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author chen
 * @date 2019/7/23
 * @email 15218979950@163.com
 * @description 对异常进行返回处理
 */
@RestControllerAdvice
@RequestMapping("/**")
public class ExceptionController {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSize;

    // 捕捉shiro的异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ShiroException.class)
    public Result handle401(ShiroException e) {
        return Result.error("401", e.getMessage());
    }

    // 捕捉shiro的异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthenticatedException.class)
    public Result handle401(UnauthenticatedException e) {
        return Result.error("401", "你没有权限访问");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = TokenExpiredException.class)
    public Result handler(TokenExpiredException e) throws IOException {
        return Result.error(String.valueOf(HttpStatus.BAD_REQUEST.value()),"token已经过期，请重新登录");
    }
    // 处理文件大小超出限制的异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        // 在适当的位置打印值
        System.out.println("Max File Size: " + maxFileSize);
        System.out.println("Max Request Size: " + maxRequestSize);
        //long maxFileSize = e.getMaxUploadSize(); // 获取上传文件的最大限制大小
        return Result.error("400", "文件大小超出限制，请上传不超过 " + maxFileSize + " 的文件。");
    }


}