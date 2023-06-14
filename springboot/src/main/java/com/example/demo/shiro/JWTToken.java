package com.example.demo.shiro;

import org.apache.shiro.authc.AuthenticationToken;

// AuthenticationToken 是shiro框架的。
//封装token来替换Shiro原生Token
//shiro默认supports的是UsernamePasswordToken，而我们现在采用了jwt的方式，所以这里我们自定义一个JwtToken，来完成shiro的supports方法。
public class JWTToken implements AuthenticationToken {
    private String token;
    public JWTToken(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }
    @Override
    public Object getPrincipal() {
        return token;
    }
    @Override
    public Object getCredentials() {
        return token;
    }
}
