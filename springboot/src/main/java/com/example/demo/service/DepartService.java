package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.Result;
import com.example.demo.common.DepartQueryParam;
import com.example.demo.entity.Depart;

public interface DepartService extends IService<Depart> {
    /**
     * 添加部门
     * @param depart
     * @return
     */
    Result insertDepart(Depart depart);
    /**
     * 分页查询部门列表
     * @param index 当前页
     * @param size 每页显示数量
     * @param departQueryParam 筛选条件对象
     * @return
     */
    Result findDepartList(Integer index, Integer size, DepartQueryParam departQueryParam);

    Result<?> modifyDepart(Depart depart);

    Result<?> deleteDepart(Integer id);
}
