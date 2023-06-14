package com.example.demo.controller;

import cn.hutool.json.JSONObject;
import com.example.demo.common.BusinessType;
import com.example.demo.common.MyLog;
import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

@RestController
public class LoginController {

    @Resource
    UserService userService;
    @PostMapping("/login")
    @MyLog(title = "登录", operParam = "#{user.username},#{user.password}", businessType = BusinessType.OTHER)
    public Result<?> login(@RequestBody User user, HttpServletResponse response) throws UnsupportedEncodingException {
        JSONObject json = new JSONObject();

        /** 验证userName，passWord和数据库中是否一致，如不一致，直接return ResultTool.errer(); 【这里省略该步骤】*/
        Result<?> oneUser = userService.getOneUser(user);
        if (!Objects.equals(oneUser.code, "0")){
            return oneUser;
        }else {
            User res = (User)oneUser.getData();
            Integer userId = res.getId();
            // 将userId存储在Cookie中
            Cookie cookie = new Cookie("userId", userId.toString());
            cookie.setMaxAge(180 * 60); // 设置Cookie的过期时间为30分钟
            cookie.setPath("/");
            response.addCookie(cookie);

            //session.setAttribute("userId", userId);
            //String token = jwtConfig.createToken(userId);
            //六一修改
            String token = JwtUtils.createToken(user.getUsername(),user.getPassword());
            if (!StringUtils.isEmpty(token)) {
                json.put("token", token);
            }
            System.out.println(json);
            return Result.success(json);
        }
        // 这里模拟通过用户名和密码，从数据库查询userId
        // 这里把userId转为String类型，实际开发中如果subject需要存userId，则可以JwtConfig的createToken方法的参数设置为Long类型
    }
    @RequestMapping(path = "/unauthorized/{message}")
    public Result unauthorized(@PathVariable String message) throws UnsupportedEncodingException {
        System.out.println("测试打印***********************************");
        return Result.error("401",message);
    }

    @PostMapping("/logout")
    @MyLog(title = "退出登录", businessType = BusinessType.OTHER)
    public Result<?> logout(HttpServletResponse response) {
        // 注销用户认证信息

        Cookie cookie = new Cookie("userId", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 构造响应数据
        return Result.success();
    }

}
