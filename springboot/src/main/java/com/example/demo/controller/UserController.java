package com.example.demo.controller;

import cn.hutool.json.JSONObject;
import com.example.demo.common.*;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

@RestController
@RequestMapping("/user")

public class UserController {

    @Resource
    UserService userService;
    //六一测试修改
    //@Resource
    //private JwtConfig jwtConfig;
    @Resource
    private JwtUtils jwtUtils;
    //新增用户
    //@RequiresRoles("superuser")
    @RequiresPermissions("userManager:insertUser")
    @PostMapping
    @MyLog(title = "新增用户", operParam = "#{user}", businessType = BusinessType.INSERT)
    public Result<?> save(@RequestBody User user) {
        return userService.insertUser(user);
    }


    // 删除单个用户
    //@RequiresRoles("superuser")
    @RequiresPermissions("userManager:deleteUser")
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
    //@RequiresRoles("superuser")
    @RequiresPermissions("userManager:list")
    @PostMapping("{index}/{size}")

    public Result<?> findUserList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) UserQueryParam userQueryParam) {

        Result<User> result=  userService.findUserList(index, size, userQueryParam);
        return result;
    }


    // 根据用户编号查询用户信息
    //@RequiresRoles(logical = Logical.OR, value = {"superuser","department_admin" ,"designer","regular_user"})
    @GetMapping("{id}")
    public Result<?> getUserInfoById(@PathVariable(value = "id") Integer id) {
        return userService.getUserInfoById(id);
    }


    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer","regular_user"})
    @PostMapping("/findListByName")
    public Result<?> findListByName(@RequestBody String userName) {
        System.out.println("userName:" + userName.substring(1, userName.length() - 1));
        Result<?> result=  userService.findListByName(userName.substring(1, userName.length() - 1));
        System.out.println("this is pull test code...");
        return result;
    }
    /**
     * 根据请求头的token获取userId
     *
     * @param request
     * @return
     */
    //@RequiresRoles(logical = Logical.OR, value = {"superuser","department_admin", "designer","regular_user"})
    @GetMapping("/getUserInfo")
    //public Result<?> getUserInfo(HttpServletRequest request) {
    //    String usernameFromToken = jwtConfig.getUsernameFromToken(request.getHeader("token"));
    //    return Result.success(usernameFromToken);
    //}
    public Result<?> getUserInfo(HttpServletRequest request) {
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        return Result.success(userByUsername.getId());
    }

    /**
     * 重置密码
     *
     * @param
     * @return
     */
    //@RequiresRoles(logical = Logical.OR, value = {"superuser","designer","regular_user"})
    @GetMapping("/resetPassword/{id}")
    public Result<?> resetPassword(@PathVariable(value = "id") Integer id) {
        return Result.success(userService.resetPassword(id));
    }


    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        return userService.insertUser(user);
    }

    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer","regular_user"})
    @PostMapping("/confirmPassword")
    public Result<?> confirmPassword(@RequestBody User user) {
        return userService.confirmPassword(user);
    }


}
