package com.example.demo.controller;

import com.example.demo.common.DrawingQueryParam;
import com.example.demo.common.Result;

import com.example.demo.entity.Drawing;

import com.example.demo.mapper.DrawingMapper;

import com.example.demo.service.DrawingService;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/drawing")
public class DrawingController {
    @Resource
    DrawingService drawingService;
    //新增图纸信息
    @PostMapping
    public Result<?> save(@RequestBody Drawing drawing) {
        return drawingService.insertDrawing(drawing);
    }
    //此处测试合并代码
    // 删除单个图纸信息
    @DeleteMapping("{id}")
    public Result<?> deleteDrawing(@PathVariable(value = "id") Integer id) {
        return drawingService.deleteDrawing(id);
    }
    // 编辑图纸信息
    @PutMapping("")
    public Result<?> modifyDrawing(@RequestBody Drawing drawing) {
        return drawingService.modifyDrawing(drawing);
    }
    // 查询图纸信息列表
    @PostMapping("{index}/{size}")
    public Result<?> findDrawingList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) DrawingQueryParam drawingQueryParam) {

        Result<?> result=  drawingService.findDrawingList(index, size, drawingQueryParam);
        System.out.println("this is gitee test code...");
        return result;
    }
    //根据料号查询
    @GetMapping("/getFilePath")
    public Result<?> getFilePath(@RequestParam String productNo) {
        Result<?> filePaths = drawingService.getFilePath(productNo);
        return filePaths;
    }
}
