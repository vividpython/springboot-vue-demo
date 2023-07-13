package com.example.demo.controller;

import com.example.demo.common.BusinessType;
import com.example.demo.common.RoleQueryParam;
import com.example.demo.common.MyLog;
import com.example.demo.common.Result;
import com.example.demo.entity.Role;
import com.example.demo.service.RoleService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Resource
    RoleService roleService;

    //@Resource
    //private JwtConfig jwtConfig;

    //新增角色
    //@RequiresRoles("superuser")
    @RequiresPermissions("roleManager:insertRole")
    @PostMapping
    @MyLog(title = "新增部門", operParam = "#{role}", businessType = BusinessType.INSERT)
    public Result<?> save(@RequestBody Role role) {
        return roleService.insertRole(role);
    }


    //删除单个角色
    //@RequiresRoles("superuser")
    @RequiresPermissions("roleManager:deleteRole")
    @DeleteMapping("{id}")
    @MyLog(title = "删除用户", operParam = "#{id}", businessType = BusinessType.DELETE)
    public Result<?> deleteRole(@PathVariable(value = "id") Integer id) {
        return roleService.deleteRole(id);
    }
    // 编辑角色
    //@RequiresRoles("superuser")
    @RequiresPermissions("roleManager:editRole")
    @PutMapping("")
    @MyLog(title = "编辑用户", operParam = "#{role}", businessType = BusinessType.UPDATE)
    public Result<?> modifyRole(@RequestBody Role role) {
        return roleService.modifyRole(role);
    }
    // 查询角色列表
    //@RequiresRoles("superuser")
    @RequiresPermissions("roleManager:list")
    @PostMapping("{index}/{size}")

    public Result<?> findRoleList(@PathVariable(value = "index") Integer index,
                                  @PathVariable(value = "size") Integer size,
                                  @RequestBody(required = false) RoleQueryParam roleQueryParam) {

        Result<Role> result=  roleService.findRoleList(index, size, roleQueryParam);
        return result;
    }
    //@RequiresRoles("superuser")
    @PostMapping("/findListByName")
    public Result<?> findListByName(@RequestBody String roleName) {
        System.out.println("roleName:" + roleName.substring(1, roleName.length() - 1));
        Result<?> result=  roleService.findByNameList(roleName.substring(1, roleName.length() - 1));
        System.out.println("this is pull test code...");
        return result;
    }


}
