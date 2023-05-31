package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.DepartQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Depart;
import com.example.demo.mapper.DepartMapper;
import com.example.demo.service.DepartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("departService")

@Transactional
public class DepartServiceImpl extends ServiceImpl<DepartMapper, Depart> implements DepartService {

    @Override
    public Result insertDepart(Depart depart) {
        if (depart == null) return Result.error("201","参数错误");

        // 用户名
        String name = depart.getName();
        // 构建条件对象, 查询是否已经存在用户名
        QueryWrapper<Depart> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");

        // 查询判断, 如果查询出来有数据, 则不为null
        if (this.baseMapper.selectOne(wrapper) != null) Result.error("201","该部门名已存在");

        // 执行插入数据操作
        return this.baseMapper.insert(depart) == 0 ? Result.error("201","添加部门失败") : Result.success();
    }

    @Override
    public Result findDepartList(Integer index, Integer size, DepartQueryParam departQueryParam) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }
        //}else if (size > 10) {
        //    return Result.error().message("一次最多10条数据");
        //}



        // 构建分页对象
        Page<Depart> page = new Page<>(index, size);
        // 查询
        IPage<Depart> iPage = this.baseMapper.findDepartList(page, departQueryParam);

        // 回传两个数据, 一个 userList --> 用户数据列表, 一个 total -> 总页数
        //return Result.success().data("userList", iPage.getRecords()).data("total", iPage.getTotal());
        return Result.success(iPage);
    }

    @Override
    public Result<?> modifyDepart(Depart depart) {

        if (depart == null || depart.getId() == null || depart.getId() <= 0) return Result.error("201","参数错误");
        System.out.println("updateById:"+this.baseMapper.updateById(depart));
        return this.baseMapper.updateById(depart) == 0 ? Result.error("201","编辑部门失败") : Result.success();

    }

    @Override
    public Result<?> deleteDepart(Integer id) {

        if (id == null || id <= 0) return Result.error("201","参数错误");

        return  this.baseMapper.deleteById(id) == 0 ? Result.error("201","删除失败"): Result.success();
    }
}
