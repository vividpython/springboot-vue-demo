package com.example.demo.controller;


import com.example.demo.common.NewsQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.News;
import com.example.demo.service.NewsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/news")
public class NewsController {
    @Resource
    NewsService newsService;
    //新增图纸信息
    @PostMapping
    public Result<?> saveNews(@RequestBody News news) {
        return newsService.insertNews(news);
    }

    // 删除单个图纸信息
    @DeleteMapping("{id}")
    public Result<?> deleteNews(@PathVariable(value = "id") Integer id) {
        return newsService.deleteNews(id);
    }
    // 编辑图纸信息
    @PutMapping("")
    public Result<?> modifyNews(@RequestBody News news) {
        return newsService.modifyNews(news);
    }
    // 查询图纸信息列表
    @PostMapping("{index}/{size}")
    public Result<?> findNewsList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) NewsQueryParam newsQueryParam) {

        Result<News> result=  newsService.findNewsList(index, size, newsQueryParam);
        return result;
    }
}
