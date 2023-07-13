package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.RoleQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Role;

public interface RoleService extends IService<Role> {
    /**
     * 添加部门
     * @param role
     * @return
     */
    Result insertRole(Role role);
    /**
     * 分页查询部门列表
     * @param index 当前页
     * @param size 每页显示数量
     * @param roleQueryParam 筛选条件对象
     * @return
     */
    Result findRoleList(Integer index, Integer size, RoleQueryParam roleQueryParam);

    Result<?> modifyRole(Role role);

    Result<?> deleteRole(Integer id);

    Result<?> findByNameList(String roleName);
}
