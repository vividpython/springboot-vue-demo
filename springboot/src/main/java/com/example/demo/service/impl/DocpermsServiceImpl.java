package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.DocpermsQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Docperms;
import com.example.demo.mapper.DocpermsMapper;
import com.example.demo.service.DocpermsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service("docpermsService")

@Transactional
public class DocpermsServiceImpl extends ServiceImpl<DocpermsMapper, Docperms> implements DocpermsService {

    @Override
    public Result insertDocperms(Docperms docperms) {
        if (docperms == null) return Result.error("201","参数错误");

        // 用户名
        String name = docperms.getName();
        // 构建条件对象, 查询是否已经存在用户名
        QueryWrapper<Docperms> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");

        // 查询判断, 如果查询出来有数据, 则不为null
        if (this.baseMapper.selectOne(wrapper) != null) Result.error("201","该部门名已存在");

        // 执行插入数据操作
        return this.baseMapper.insert(docperms) == 0 ? Result.error("201","添加部门失败") : Result.success();
    }

    @Override
    public Result findDocpermsList(Integer index, Integer size, DocpermsQueryParam docpermsQueryParam) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }
        //}else if (size > 10) {
        //    return Result.error().message("一次最多10条数据");
        //}



        // 构建分页对象
        Page<Docperms> page = new Page<>(index, size);
        // 查询
        IPage<Docperms> iPage = this.baseMapper.findDocpermsList(page, docpermsQueryParam);

        // 回传两个数据, 一个 userList --> 用户数据列表, 一个 total -> 总页数
        //return Result.success().data("userList", iPage.getRecords()).data("total", iPage.getTotal());
        return Result.success(iPage);
    }

    @Override
    public Result findAllDocperms() {
        return Result.success(this.baseMapper.findAllDocperms());
    }

    @Override
    public Result<?> modifyDocperms(Docperms docperms) {

        if (docperms == null || docperms.getId() == null || docperms.getId() <= 0) return Result.error("201","参数错误");
        System.out.println("updateById:"+this.baseMapper.updateById(docperms));
        return this.baseMapper.updateById(docperms) == 0 ? Result.error("201","编辑部门失败") : Result.success();

    }

    @Override
    public Result<?> deleteDocperms(Integer id) {

        if (id == null || id <= 0) return Result.error("201","参数错误");

        return  this.baseMapper.deleteById(id) == 0 ? Result.error("201","删除失败"): Result.success();
    }

    @Override
    public Result<?> findByNameList(String docpermsName) {
        if (docpermsName == null ) return Result.error("201","参数错误");
        return  Result.success(this.baseMapper.findByNameList(docpermsName)) ;
    }

    @Override
    public Result<?> setDocPermsByRoleId(Integer roleId,List<Integer> permissionList) {

        this.baseMapper.setDocPermsByRoleId(roleId,permissionList);
        return  Result.success() ;
    }


    @Override
    public Result<?> getPermissionsByRoleId(Integer roleid) {
        if (roleid == null || roleid <= 0) return Result.error("201","参数错误");
        return  Result.success(this.baseMapper.getPermissionsByRoleId(roleid)) ;
    }
}
