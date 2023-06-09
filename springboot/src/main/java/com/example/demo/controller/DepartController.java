package com.example.demo.controller;

import cn.hutool.json.JSONObject;
import com.example.demo.common.*;
import com.example.demo.entity.Depart;
import com.example.demo.entity.Document;
import com.example.demo.service.DepartService;
import org.apache.commons.collections.MapUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/depart")
public class DepartController {
    @Resource
    DepartService departService;

    //@Resource
    //private JwtConfig jwtConfig;

    //新增部门
    @RequiresRoles("admin")
    @PostMapping
    @MyLog(title = "新增部門", operParam = "#{depart}", businessType = BusinessType.INSERT)
    public Result<?> save(@RequestBody Depart depart) {
        return departService.insertDepart(depart);
    }


    // 删除单个部门
    @RequiresRoles("admin")
    @DeleteMapping("{id}")
    @MyLog(title = "删除用户", operParam = "#{id}", businessType = BusinessType.DELETE)
    public Result<?> deleteDepart(@PathVariable(value = "id") Integer id) {
        return departService.deleteDepart(id);
    }
    // 编辑部门
    @RequiresRoles("admin")
    @PutMapping("")
    @MyLog(title = "编辑用户", operParam = "#{depart}", businessType = BusinessType.UPDATE)
    public Result<?> modifyDepart(@RequestBody Depart depart) {
        return departService.modifyDepart(depart);
    }
    // 查询部门列表
    @RequiresRoles("admin")
    @PostMapping("{index}/{size}")

    public Result<?> findDepartList(@PathVariable(value = "index") Integer index,
                                  @PathVariable(value = "size") Integer size,
                                  @RequestBody(required = false) DepartQueryParam departQueryParam) {

        Result<Depart> result=  departService.findDepartList(index, size, departQueryParam);
        return result;
    }
    @RequiresRoles("admin")
    @PostMapping("/findListByName")
    public Result<?> findListByName(@RequestBody String departName) {
        System.out.println("departName:" + departName.substring(1, departName.length() - 1));
        Result<?> result=  departService.findByNameList(departName.substring(1, departName.length() - 1));
        System.out.println("this is pull test code...");
        return result;
    }

}
