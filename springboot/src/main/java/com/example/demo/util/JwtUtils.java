package com.example.demo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
 * @program:
 * @description:
 * @author: sun jingchun
 * @create: 2021-10-26 16:47
 **/
@Component
public class JwtUtils {


    /**
     * token密钥
     */
    private static final String SECRET = "889556654";
    /**
     * @description: 生产token
     * @param username:
 * @param password:
     * @return java.lang.String
     * @author: Zheng
     * @date: 2023/6/2 9:26
     */

    public static String createToken(String username, String password) throws UnsupportedEncodingException {
        //设置token时间 三小时
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.HOUR, 3);
        Date date = nowTime.getTime();


        //密文生成
        String token = JWT.create()
                //私有声明
                .withClaim("username", username)
                .withClaim("password", password)
                //过期时间
                .withExpiresAt(date)
                //签发时间
                .withIssuedAt(new Date())
                //签名
                .sign(Algorithm.HMAC256(SECRET));
        return token;
    }

    /**
     * 验证token的有效性
     * 1、token的header和payload是否没改过；2、没有过期
     */
    public static boolean verify(String token) {
        try {
            //解密
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            verifier.verify(token);
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    /**
     * 获取token列名
     * **/
    /**
     * 通过载荷名字获取载荷的值
     * //无需解密也可以获取token的信息 获取用户名
     */

    public static String getClaim(String token, String name) {
        String claim = null;
        try {
            claim = JWT.decode(token).getClaim(name).asString();
        } catch (Exception e) {
            return "getClaimFalse";
        }
        return claim;
    }
}
