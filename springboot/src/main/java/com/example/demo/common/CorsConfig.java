package com.example.demo.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    //当前跨域请求最大有效时长。这里默认1天
    private static final long HAX_AGE = 24* 60* 60;
    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");// 1设置访问源地址
        corsConfiguration.addAllowedHeader("* ");//2 设置访问源请求头
        corsConfiguration.addAllowedMethod("*");//3设置访问源请求方法
        corsConfiguration.addAllowedHeader("Access-Control-Request-Headers");
        corsConfiguration.addAllowedHeader("Access-Control-Request-Method");
        corsConfiguration.addAllowedHeader("Origin");
        corsConfiguration.addAllowedHeader("Content-Type");
        corsConfiguration.addAllowedHeader("Accept");
        corsConfiguration.addAllowedHeader("token");
        corsConfiguration.addAllowedMethod("POST");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setMaxAge(HAX_AGE);
        return corsConfiguration;
    }
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration( "/**",buildConfig());//4对接口配置跨域设置
        return new CorsFilter(source);
    }

}
