package com.example.demo.controller;

import cn.hutool.json.JSONObject;
import com.example.demo.common.*;
import com.example.demo.entity.Depart;
import com.example.demo.service.DepartService;
import com.example.demo.utils.ThreadLocalUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

@RestController
@RequestMapping("/depart")
public class DepartController {
    @Resource
    DepartService departService;

    @Resource
    private JwtConfig jwtConfig;

    //新增用户
    @PostMapping
    @MyLog(title = "新增部門", operParam = "#{depart}", businessType = BusinessType.INSERT)
    public Result<?> save(@RequestBody Depart depart) {
        return departService.insertDepart(depart);
    }


    // 删除单个用户
    @DeleteMapping("{id}")
    @MyLog(title = "删除用户", operParam = "#{id}", businessType = BusinessType.DELETE)
    public Result<?> deleteDepart(@PathVariable(value = "id") Integer id) {
        return departService.deleteDepart(id);
    }
    // 编辑用户
    @PutMapping("")
    @MyLog(title = "编辑用户", operParam = "#{depart}", businessType = BusinessType.UPDATE)
    public Result<?> modifyDepart(@RequestBody Depart depart) {
        return departService.modifyDepart(depart);
    }
    // 查询用户列表
    @PostMapping("{index}/{size}")

    public Result<?> findDepartList(@PathVariable(value = "index") Integer index,
                                  @PathVariable(value = "size") Integer size,
                                  @RequestBody(required = true) DepartQueryParam departQueryParam) {

        Result<Depart> result=  departService.findDepartList(index, size, departQueryParam);
        return result;
    }

}
