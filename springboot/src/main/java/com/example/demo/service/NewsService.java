package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.NewsQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.News;

public interface NewsService extends IService<News> {
    /**
     * 添加用户
     * @param news
     * @return
     */
    Result insertNews(News news);
    /**
     * 分页查询图纸列表
     * @param index 当前页
     * @param size 每页显示数量
     * @param newsQueryParam 筛选条件对象
     * @return
     */
    Result findNewsList(Integer index, Integer size, NewsQueryParam newsQueryParam);

    Result<?> modifyNews(News news);

    Result<?> deleteNews(Integer id);


}
