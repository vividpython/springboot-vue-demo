package com.example.demo.controller;

import cn.hutool.json.JSONObject;
import com.example.demo.common.*;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.utils.ThreadLocalUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.net.http.HttpResponse;
import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    @Resource
    private JwtConfig jwtConfig;

    //新增用户
    @PostMapping
    @MyLog(title = "新增用户", operParam = "#{user}", businessType = BusinessType.INSERT)
    public Result<?> save(@RequestBody User user) {
        return userService.insertUser(user);
    }


    // 删除单个用户
    @DeleteMapping("{id}")
    @MyLog(title = "删除用户", operParam = "#{id}", businessType = BusinessType.DELETE)
    public Result<?> deleteUser(@PathVariable(value = "id") Integer id) {
        return userService.deleteUser(id);
    }
    // 编辑用户
    @PutMapping("")
    @MyLog(title = "编辑用户", operParam = "#{user}", businessType = BusinessType.UPDATE)
    public Result<?> modifyUser(@RequestBody User user, HttpSession session) {
        //Integer userId = (Integer) session.getAttribute("userId");
        return userService.modifyUser(user);
    }
    // 查询用户列表
    @PostMapping("{index}/{size}")

    public Result<?> findUserList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) UserQueryParam userQueryParam) {

        Result<User> result=  userService.findUserList(index, size, userQueryParam);
        return result;
    }


    @PostMapping("/login")
    @MyLog(title = "登录", operParam = "#{user.username},#{user.password}", businessType = BusinessType.OTHER)
    public Result<?> login(@RequestBody User user, HttpServletResponse response) {
        JSONObject json = new JSONObject();

        /** 验证userName，passWord和数据库中是否一致，如不一致，直接return ResultTool.errer(); 【这里省略该步骤】*/
        Result<?> oneUser = userService.getOneUser(user);
        if (!Objects.equals(oneUser.code, "0")){
            return oneUser;
        }else {
            User res = (User)oneUser.getData();
            Integer userId = res.getId();
            //ThreadLocalUtils.set("userId",userId);
            // 将userId存储在Cookie中
            Cookie cookie = new Cookie("userId", userId.toString());
            cookie.setMaxAge(180 * 60); // 设置Cookie的过期时间为30分钟
            cookie.setPath("/");
            response.addCookie(cookie);

            //session.setAttribute("userId", userId);
            String token = jwtConfig.createToken(userId);

            if (!StringUtils.isEmpty(token)) {
                json.put("token", token);
            }
            System.out.println(json);
            return Result.success(json);
        }
        // 这里模拟通过用户名和密码，从数据库查询userId
        // 这里把userId转为String类型，实际开发中如果subject需要存userId，则可以JwtConfig的createToken方法的参数设置为Long类型
    }

    @PostMapping("/logout")
    @MyLog(title = "退出登录", businessType = BusinessType.OTHER)
    public Result<?> logout(HttpServletResponse response) {
        // 注销用户认证信息
        ThreadLocalUtils.removeByKey("userId");

        Cookie cookie = new Cookie("userId", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 构造响应数据
        return Result.success();
    }

    // 根据用户编号查询用户信息
    @GetMapping("{id}")
    public Result<?> getUserInfoById(@PathVariable(value = "id") Integer id) {
        return userService.getUserInfoById(id);
    }

    /**
     * 根据请求头的token获取userId
     *
     * @param request
     * @return
     */
    @GetMapping("/getUserInfo")
    public Result<?> getUserInfo(HttpServletRequest request) {
        String usernameFromToken = jwtConfig.getUsernameFromToken(request.getHeader("token"));
        return Result.success(usernameFromToken);
    }
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        return userService.insertUser(user);
    }


    @PostMapping("/confirmPassword")
    public Result<?> confirmPassword(@RequestBody User user) {
        return userService.confirmPassword(user);
    }


}
