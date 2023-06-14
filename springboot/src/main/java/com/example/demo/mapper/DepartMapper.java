package com.example.demo.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.DepartQueryParam;
import com.example.demo.common.UserQueryParam;
import com.example.demo.entity.Depart;
import com.example.demo.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@DS("mysql")
public interface DepartMapper extends BaseMapper<Depart> {
    /**
     * 查询部门列表
     * @param page 分页对象
     * @param departQueryParam 筛选条件
     * @return
     */
    IPage<Depart> findDepartList(Page<Depart> page, DepartQueryParam departQueryParam);

    List<Depart> findByNameList(String departName);
}
