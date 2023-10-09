package com.example.demo.common;

import cn.hutool.jwt.Claims;
import com.example.demo.util.JwtUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineCounter {
    //每次打开此类只初始化一次countMap
    private static Map countMap = new ConcurrentHashMap<String,Object>();

    /**
     * @auther: Arong
     * @description: 解析token并且将数据插入CountMap中
     * @param token
     * @return: void
     * @date: 2019/1/22 17:44
     */
    public void insertToken(String token){
        //获得当前时间(毫秒)
        long currentTime = System.currentTimeMillis();
        //解析token，获得签发时间
        Date issuedAt = null;
        try {
            issuedAt = JwtUtils.getIssuedAt(token);
        } catch (Exception e) {
            throw new RuntimeException("token不存在或已过期");
        }
        //以签发时间为key。当前时间+60s为value存入countMap中
        countMap.put(issuedAt.toString(),currentTime+60*1000);
    }

    /**
     * @auther: Arong
     * @description: 获取当前在线用户数
     * @param
     * @return: java.lang.Integer
     * @date: 2019/1/22 17:51
     */
    public Integer getOnlineCount(){
        int onlineCount = 0;
        //获取countMap的迭代器
        Iterator iterator = countMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String,Object>  entry = (Map.Entry<String, Object>) iterator.next();
            Long value = (Long) entry.getValue();
            if (value > System.currentTimeMillis()) {
                //过期时间大于当前时间则没有过期
                onlineCount++;
            }

        }

        return onlineCount;
    }
}
