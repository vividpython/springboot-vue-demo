package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.common.DrawingQueryParam;
import com.example.demo.entity.Drawing;
import com.example.demo.entity.EditDrawing;
import com.example.demo.mapper.DrawingMapper;
import com.example.demo.service.DrawingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service("drawingService")
@Transactional
public class DrawingServiceImpl extends ServiceImpl<DrawingMapper,Drawing> implements DrawingService {
    //插入用户
    @Override
    public Result<?> insertDrawing(Drawing drawing) {
        if (drawing == null || drawing.getProductNo() == null || drawing.getDrawingType() == null|| drawing.getDrawingVersion() == null) {
            return Result.error("201", "参数错误");
        }
        //如果是因为更新版本而插入数据 则要去掉带入的id
        if ( drawing.getId() != null ){
            drawing.setId(null);
        }
        if (drawing.getCreateTime() != null)
        {
            drawing.setCreateTime(null);
        }
        // 产品料号
        String productNo = drawing.getProductNo();
        // 图纸类型
        Integer drawingType = drawing.getDrawingType();
        //项目号
        String itemNo = drawing.getItemNo();
        //图纸版本
        String drawingVersion = drawing.getDrawingVersion();


        // 构建条件对象, 查询是否已经存在用户名
        QueryWrapper<Drawing> wrapper = new QueryWrapper<>();

        wrapper.select("id");
        wrapper.eq("product_no", productNo);
        wrapper.eq("drawing_type", drawingType);
        wrapper.eq("item_no", itemNo);
        wrapper.eq("drawing_version", drawingVersion);
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
    public Result<?> modifyDrawing(Drawing drawing, EditDrawing editDrawing) {
        if (drawing == null || drawing.getId() == null || drawing.getId() <= 0) return Result.error("201","参数错误");
        String old_drawingName = editDrawing.getDrawingName();
        Integer old_drawingType = editDrawing.getDrawingType();
        String drawingName = drawing.getDrawingName();
        Integer drawingType = drawing.getDrawingType();

        //交换数据
        //现在的editForm是被编辑的内容
        editDrawing.setDrawingName(drawingName);
        editDrawing.setDrawingType(drawingType);
        //重新组装数据 现在的drawing是修改前的数据
        drawing.setDrawingName(old_drawingName);
        drawing.setDrawingType(old_drawingType);

        // 判断绘图名称和绘图类型是否发生变化，如果没有变化则不需要更新
        boolean needUpdateName = !drawingName.equals(old_drawingName);
        boolean needUpdateType = !drawingType.equals(old_drawingType);

        if (needUpdateName || needUpdateType) {
            // 根据当前被编辑的实体查询历史版本绘图对象列表，并将需要更新的绘图对象进行更新操作
            List<Drawing> drawingsByEditDrawing = findDrawingsByEditDrawing(drawing);
            if (drawingsByEditDrawing != null && !drawingsByEditDrawing.isEmpty()){
                for (Drawing draw : drawingsByEditDrawing) {
                    if (needUpdateName) {
                        draw.setDrawingName(drawingName);
                    }
                    if (needUpdateType) {
                        draw.setDrawingType(drawingType);
                    }
                    this.baseMapper.updateById(draw);
                }
            }

        }


        //最后对本条信息进行更新操作的时候 要把drawing对象的可被修改的两个字段重新装载
        drawing.setDrawingName(drawingName);
        drawing.setDrawingType(drawingType);

        return this.baseMapper.updateById(drawing) == 0 ? Result.error("201","编辑用户失败") : Result.success();
    }
    //删除
    @Override
    public Result<?> deleteDrawing(Integer id) {
        if (id == null || id <= 0) return Result.error("201","参数错误");

        return  this.baseMapper.deleteById(id) == 0 ? Result.error("201","删除失败"): Result.success();
    }

    @Override
    public Result<?> getDrawingById(Integer id) {
        if (id == null || id <= 0)
            return Result.error("202","参数错误");

        return Result.success(this.baseMapper.selectById(id));
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

    @Override
    public String getNowDrawingVersion(Integer id) {
        QueryWrapper<Drawing> wrapper = new QueryWrapper<>();
        wrapper.select("drawing_version").eq("id", id).orderByDesc("create_time").last("limit 1");
        Drawing drawing = this.baseMapper.selectOne(wrapper);
        if (drawing == null) {
            return null;
        }
        return drawing.getDrawingVersion();
    }

    @Override
    public List<Drawing> findDrawingsByEditDrawing(Drawing drawing) {
        if (drawing == null || drawing.getId() == null || drawing.getId() <= 0) {
            return null;
        }
        String productNo = drawing.getProductNo();
        String drawingName = drawing.getDrawingName();
        Integer drawingType = drawing.getDrawingType();
        String itemNo = drawing.getItemNo();

        // 根据 productNo、drawingName、drawingType、itemNo 等信息查找当前绘图对象所属版本的编号 version
        String version = drawing.getDrawingVersion();
        if (StringUtils.isBlank(version)) {
            Drawing currentDrawing = this.baseMapper.selectById(drawing.getId());
            if (currentDrawing == null) {
                return null;
            }
            version = currentDrawing.getDrawingVersion();
        }
        System.out.println("itemNo:"+itemNo);
        if (itemNo == null){
            // 查找该版本之前的所有绘图对象
            List<Drawing> drawings = this.baseMapper.selectList(new QueryWrapper<Drawing>()
                    .eq("product_no", productNo)
                    .eq("drawing_name", drawingName)
                    .eq("drawing_type", drawingType)
                    .isNull("item_no")
                    .ne("drawing_version", version));
            if (CollectionUtils.isEmpty(drawings)) {
                return null;
            }
            return drawings;
        }else{
            // 查找该版本之前的所有绘图对象
            List<Drawing> drawings = this.baseMapper.selectList(new QueryWrapper<Drawing>()
                    .eq("product_no", productNo)
                    .eq("drawing_name", drawingName)
                    .eq("drawing_type", drawingType)
                    .eq("item_no", itemNo)
                    .ne("drawing_version", version));
            if (CollectionUtils.isEmpty(drawings)) {
                return null;
            }
            return drawings;
        }





    }

    @Override
    public Result<?> findHistoryList(Integer index, Integer size, Drawing drawing) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }

        // 构建分页对象
        Page<Drawing> page = new Page<>(index, size);
        // 查询
        IPage<Drawing> iPage = this.baseMapper.findHistoryList(page, drawing);

        Result result = new Result<>(iPage);
        return Result.success(iPage);
    }

    @Override
    public Result<?> deleteDrawingMore(List<Integer> ids) {
        if (ids.size() == 0) return Result.error("201","参数错误");
        return this.baseMapper.deleteBatchIds(ids) != ids.size() ? Result.error("201","删除失败") : Result.success();
    }

    @Override
    public List<Drawing> findDrawingMore(List<Integer> ids) {
        List<Drawing> drawings = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            drawings = this.baseMapper.findDrawingMore(ids);
        }
        return drawings;
    }


}
