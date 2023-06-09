package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.DrawingQueryParam;
import com.example.demo.entity.Drawing;
import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrawingMapper extends BaseMapper<Drawing> {
    /**
     * 查询用户列表
     * @param page 分页对象
     * @param drawingQueryParam 筛选条件
     * @return
     */
    IPage<Drawing> findDrawingList(Page<Drawing> page, DrawingQueryParam drawingQueryParam);
    List<Drawing> findDrawingListByPN(String product_no);
    IPage<Drawing> findHistoryList(Page<Drawing> page, Drawing drawing);

    List<Drawing> findDrawingMore(@Param("ids") List<Integer> ids);

}
