package com.example.demo.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.RoleQueryParam;
import com.example.demo.entity.Role;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@DS("mysql")
public interface RoleMapper extends BaseMapper<Role> {
    /**
     * 查询部门列表
     * @param page 分页对象
     * @param roleQueryParam 筛选条件
     * @return
     */
    IPage<Role> findRoleList(Page<Role> page, RoleQueryParam roleQueryParam);

    List<Role> findByNameList(String roleName);


}
