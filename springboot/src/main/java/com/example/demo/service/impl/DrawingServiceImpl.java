package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.common.DrawingQueryParam;
import com.example.demo.entity.Drawing;
import com.example.demo.mapper.DrawingMapper;
import com.example.demo.service.DrawingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("drawingService")
@Transactional
public class DrawingServiceImpl extends ServiceImpl<DrawingMapper,Drawing> implements DrawingService {
    //插入用户
    @Override
    public Result<?> insertDrawing(Drawing drawing) {
        if (drawing == null || drawing.getProductNo() == null || drawing.getDrawingType() == null) {
            return Result.error("201", "参数错误");
        }
        // 产品料号
        String productNo = drawing.getProductNo();
        // 图纸类型
        Integer drawingType = drawing.getDrawingType();
        // 构建条件对象, 查询是否已经存在用户名
        QueryWrapper<Drawing> wrapper = new QueryWrapper<>();

        wrapper.select("id");
        wrapper.eq("product_no", productNo);
        wrapper.eq("drawing_type", drawingType);
        wrapper.last("limit 1");

        // 查询判断, 如果查询出来有数据, 则不为null
        if (this.baseMapper.selectOne(wrapper) != null)
        {
            return Result.error("201","该记录已存在");
        }
        else {
            // 执行插入数据操作
            return this.baseMapper.insert(drawing) == 0 ? Result.error("201","添加用户失败") : Result.success();
        }
    }
    //查询列表
    @Override
    public Result<?> findDrawingList(Integer index, Integer size, DrawingQueryParam drawingQueryParam) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }
        //}else if (size > 10) {
        //    return Result.error().message("一次最多10条数据");
        //}

        // 构建分页对象
        Page<Drawing> page = new Page<>(index, size);
        // 查询
        IPage<Drawing> iPage = this.baseMapper.findDrawingList(page, drawingQueryParam);

        // 回传两个数据, 一个 drawingList --> 用户数据列表, 一个 total -> 总页数
        //return Result.success().data("drawingList", iPage.getRecords()).data("total", iPage.getTotal());
        Result result = new Result<>(iPage);
        return Result.success(iPage);
    }
    //修改
    @Override
    public Result<?> modifyDrawing(Drawing drawing) {
        if (drawing == null || drawing.getId() == null || drawing.getId() <= 0) return Result.error("201","参数错误");

        return this.baseMapper.updateById(drawing) == 0 ? Result.error("201","编辑用户失败") : Result.success();
    }
    //删除
    @Override
    public Result<?> deleteDrawing(Integer id) {
        if (id == null || id <= 0) return Result.error("201","参数错误");

        return  this.baseMapper.deleteById(id) == 0 ? Result.error("201","删除失败"): Result.success();
    }

    @Override
    public Result<?> getOneDrawing(Drawing drawing) {
        if (drawing == null || drawing.getProductNo() == null || drawing.getDrawingType() == null) {
            return Result.error("201", "参数错误");
        }
        // 用户名
        QueryWrapper<Drawing> wrapper = new QueryWrapper<>();
        wrapper.eq("username", drawing.getProductNo());
        wrapper.eq("password", drawing.getDrawingType());
        Drawing res =  this.baseMapper.selectOne(wrapper);
        if (res == null){
            return Result.error("201","用户名或密码错误");
        }
        return Result.success(res);
    }

    @Override
    public Result<?> getFilePath(String productNo) {
        if (productNo == null) {
            return Result.error("201", "参数错误");
        }
        // 用户名
        List<Drawing> drawingListByPN = this.baseMapper.findDrawingListByPN(productNo);

        return Result.success(drawingListByPN);
        //return null;
    }


}
