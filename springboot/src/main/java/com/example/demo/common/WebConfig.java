package com.example.demo.common;


import com.example.demo.interceptor.TokenInterceptor;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.Resource;
import javax.servlet.MultipartConfigElement;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    //@Resource
    //private TokenInterceptor tokenInterceptor ;
    //public void addInterceptors(InterceptorRegistry registry) {
    //    registry.addInterceptor(tokenInterceptor).addPathPatterns("/**");
    //}

}