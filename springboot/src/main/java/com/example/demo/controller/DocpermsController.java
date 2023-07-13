package com.example.demo.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.example.demo.common.BusinessType;
import com.example.demo.common.DocpermsQueryParam;
import com.example.demo.common.MyLog;
import com.example.demo.common.Result;
import com.example.demo.entity.Docperms;
import com.example.demo.service.DocpermsService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/docperms")
public class DocpermsController {
    @Resource
    DocpermsService docpermsService;

    //@Resource
    //private JwtConfig jwtConfig;

    //新增权限
    //@RequiresRoles("superuser")
    @RequiresPermissions("docpermsManager:insertDocperms")
    @PostMapping
    @MyLog(title = "新增权限", operParam = "#{docperms}", businessType = BusinessType.INSERT)
    public Result<?> save(@RequestBody Docperms docperms) {
        return docpermsService.insertDocperms(docperms);
    }


    // 删除单个部门
    //@RequiresRoles("superuser")
    @RequiresPermissions("docpermsManager:deleteDocperms")
    @DeleteMapping("{id}")
    @MyLog(title = "删除用户", operParam = "#{id}", businessType = BusinessType.DELETE)
    public Result<?> deleteDocperms(@PathVariable(value = "id") Integer id) {
        return docpermsService.deleteDocperms(id);
    }
    // 编辑部门
    //@RequiresRoles("superuser")
    @RequiresPermissions("docpermsManager:editDocperms")
    @PutMapping("")
    @MyLog(title = "编辑用户", operParam = "#{docperms}", businessType = BusinessType.UPDATE)
    public Result<?> modifyDocperms(@RequestBody Docperms docperms) {
        return docpermsService.modifyDocperms(docperms);
    }
    // 查询部门列表
    //@RequiresRoles("superuser")
    @RequiresPermissions("docpermsManager:list")
    @PostMapping("{index}/{size}")

    public Result<?> findDocpermsList(@PathVariable(value = "index") Integer index,
                                  @PathVariable(value = "size") Integer size,
                                  @RequestBody(required = false) DocpermsQueryParam docpermsQueryParam) {

        Result<Docperms> result=  docpermsService.findDocpermsList(index, size, docpermsQueryParam);
        return result;
    }

    //@RequiresRoles("superuser")
    @PostMapping("/allPermissions")
    public Result<?> allPermissions() {
        return (Result<?>) docpermsService.findAllDocperms();
    }

    //@RequiresRoles("superuser")
    @PostMapping("/findListByName")
    public Result<?> findListByName(@RequestBody String docpermsName) {
        System.out.println("docpermsName:" + docpermsName.substring(1, docpermsName.length() - 1));
        Result<?> result=  docpermsService.findByNameList(docpermsName.substring(1, docpermsName.length() - 1));
        System.out.println("this is pull test code...");
        return result;
    }
    /**
     * @description: 根据角色id查询角色拥有的权限
     * @param roleid:
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/7/4 14:45
     */

    //@RequiresRoles("superuser")
    @GetMapping("/getPermissionsByRoleId/{roleid}")
    public Result<?> getPermissionsByRoleId(@PathVariable(value = "roleid") Integer roleid) {
        return docpermsService.getPermissionsByRoleId(roleid);
    }

    //@RequiresRoles("superuser")
    @PostMapping("/setDocPermsByRoleId/{roleId}")
    public Result<?> setDocPermsByRoleId(@PathVariable(value = "roleId") Integer roleId,
                                         @RequestBody Map<String, Object> requestData){
        List<Integer> permissionList = new LinkedList<>();
        if (requestData.containsKey("keyStr")) {
            String keyStr = (String) requestData.get("keyStr");
            // 拆分字符串并转换为整数
            String[] keys = keyStr.split(",");
            for (String key : keys) {
                if (!key.isEmpty()) {
                    int permissionId;
                    try {
                        permissionId = Integer.parseInt(key);
                        permissionList.add(permissionId);
                    } catch (NumberFormatException e) {
                        // 处理非法格式的权限ID
                        return Result.error("201","非法权限ID：" + key);
                    }
                }
            }
        }
        return docpermsService.setDocPermsByRoleId(roleId,permissionList);
    }
}
