package com.example.demo.controller;

import com.example.demo.common.DrawingQueryParam;
import com.example.demo.common.Result;

import com.example.demo.entity.Drawing;

import com.example.demo.entity.DrawingEditForm;
import com.example.demo.entity.EditDrawing;


import com.example.demo.service.DrawingService;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/drawing")
@RequiresRoles("admin")
public class DrawingController {

    private final static Logger logger = LoggerFactory.getLogger(DrawingController.class);

    @Resource
    DrawingService drawingService;



    @GetMapping("/log")
    public String testLog() {
        logger.debug("=====测试日志debug级别打印====");
        logger.info("======测试日志info级别打印=====");
        logger.error("=====测试日志error级别打印====");
        logger.warn("======测试日志warn级别打印=====");

        // 可以使用占位符打印出一些参数信息
        String str1 = "blog.itcodai.com";
        String str2 = "blog.csdn.net/eson_15";
        logger.info("======倪升武的个人博客：{}；倪升武的CSDN博客：{}", str1, str2);
        return "success";
    }

    //新增图纸信息
    @PostMapping
    public Result<?> save(@RequestBody Drawing drawing) {

        return drawingService.insertDrawing(drawing);
    }

    // 删除单个图纸信息
    @DeleteMapping("{id}")
    public Result<?> deleteDrawing(@PathVariable(value = "id") Integer id) {
        return drawingService.deleteDrawing(id);
    }
    // 删除多个图纸
    @PostMapping("/deleteBatch")
    public Result<?> deleteDrawingMore(@RequestBody List<Integer> ids) {
        return drawingService.deleteDrawingMore(ids);
    }

    // 编辑图纸信息
    @PostMapping(value = "/edit")
    public Result<?> modifyDrawing(@RequestBody DrawingEditForm form) {
        //如果某一个图纸的图纸类型或者文件名发生了变化 需要联动地更改这个图纸的其他版本的图纸类型
        Drawing drawing = form.getDrawing();
        EditDrawing editForm = form.getEditDrawing();
        System.out.println(drawing);
        System.out.println(editForm);
        return drawingService.modifyDrawing(drawing,editForm);
    }


    // 查询图纸信息列表
    @PostMapping("{index}/{size}")
    public Result<?> findDrawingList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) DrawingQueryParam drawingQueryParam) {

        Result<?> result=  drawingService.findDrawingList(index, size, drawingQueryParam);
        System.out.println("this is pull test code...");
        return result;
    }
    //
    @PostMapping("/historyList/{index}/{size}")
    public Result<?> findHistoryList(@PathVariable(value = "index") Integer index,
                                     @PathVariable(value = "size") Integer size,
                                     @RequestBody(required = true) Drawing drawing) {

        Result<?> result=  drawingService.findHistoryList(index, size, drawing);
        System.out.println("this is pull test code...");
        return result;
    }



    // 根据用户编号查询用户信息
    @GetMapping("{id}")
    public Result<?> getDrawingInfo(@PathVariable(value = "id") Integer id) {
        return drawingService.getDrawingById(id);
    }


    //根据料号查询
    @GetMapping("/getFilePath")
    public Result<?> getFilePath(@RequestParam String productNo) {
        Result<?> filePaths = drawingService.getFilePath(productNo);
        return filePaths;
    }
}
