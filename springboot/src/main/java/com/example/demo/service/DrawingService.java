package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.Result;
import com.example.demo.common.DrawingQueryParam;
import com.example.demo.entity.Drawing;
import com.example.demo.entity.EditDrawing;

import java.util.List;

public interface DrawingService extends IService<Drawing> {
    /**
     * 添加用户
     * @param drawing
     * @return
     */
    Result insertDrawing(Drawing drawing);
    /**
     * 分页查询图纸列表
     * @param index 当前页
     * @param size 每页显示数量
     * @param DrawingQueryParam 筛选条件对象
     * @return
     */
    Result findDrawingList(Integer index, Integer size, DrawingQueryParam DrawingQueryParam);

    Result<?> modifyDrawing(Drawing drawing, EditDrawing editDrawing);

    Result<?> deleteDrawing(Integer id);

    Result<?> getDrawingById(Integer id);

    Result<?> getFilePath(String productNo);


    String getNowDrawingVersion(Integer id);


    List<Drawing> findDrawingsByEditDrawing(Drawing drawing);

    Result<?> findHistoryList(Integer index, Integer size, Drawing drawing);

    Result<?> deleteDrawingMore(List<Integer> ids);


    List<Drawing> findDrawingMore(List<Integer> ids);
}
