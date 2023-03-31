package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.NewsQueryParam;
import com.example.demo.entity.Drawing;
import com.example.demo.entity.News;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsMapper extends BaseMapper<News> {
    /**
     * 查询用户列表
     * @param page 分页对象
     * @param newsQueryParam 筛选条件
     * @return
     */
    IPage<News> findNewsList(Page<News> page, NewsQueryParam newsQueryParam);
}
