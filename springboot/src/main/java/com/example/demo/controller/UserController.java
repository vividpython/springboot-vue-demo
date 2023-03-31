package com.example.demo.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.JwtConfig;
import com.example.demo.common.Result;
import com.example.demo.common.UserQueryParam;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
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
    public Result<?> save(@RequestBody User user) {
        return userService.insertUser(user);
    }


    // 删除单个用户
    @DeleteMapping("{id}")
    public Result<?> deleteUser(@PathVariable(value = "id") Integer id) {
        return userService.deleteUser(id);
    }
    // 编辑用户
    @PutMapping("")
    public Result<?> modifyUser(@RequestBody User user) {
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
    //@GetMapping
    //public Result<?> findUserTest(@RequestParam(defaultValue = "1") Integer index,
    //                              @RequestParam(defaultValue = "10") Integer size,
    //                              @RequestParam(defaultValue = "") String search) {
    //    LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
    //    if (StrUtil.isNotBlank(search)) {
    //        wrapper.like(User::getNickName, search);
    //    }
    //    Page<User> userPage = userMapper.selectPage(new Page<User>(index, size), wrapper);
    //    return Result.success(userPage);
    //
    //}
    //@PostMapping("/login")
    //public Result<?> login(@RequestBody User user) {
    //    return userService.getOneUser(user);
    //}
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) {
        JSONObject json = new JSONObject();

        /** 验证userName，passWord和数据库中是否一致，如不一致，直接return ResultTool.errer(); 【这里省略该步骤】*/
        Result<?> oneUser = userService.getOneUser(user);
        if (!Objects.equals(oneUser.code, "0")){
            return oneUser;
        }else {
            User res = (User)oneUser.getData();
            Integer userId = res.getId();
            String token = jwtConfig.createToken(userId);
            if (!StringUtils.isEmpty(token)) {
                json.put("token", token);
            }
            //// 构造返回结果对象
            //Map<String, Object> map = new HashMap<>();
            //map.put("token", token);
            //map.put("user", res);
            //return Result.success(map);
            return Result.success(json);
        }
        // 这里模拟通过用户名和密码，从数据库查询userId
        // 这里把userId转为String类型，实际开发中如果subject需要存userId，则可以JwtConfig的createToken方法的参数设置为Long类型
    }
    // 根据用户编号查询用户信息
    @GetMapping("{id}")
    public Result<?> getUserInfo(@PathVariable(value = "id") Integer id) {
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
}
