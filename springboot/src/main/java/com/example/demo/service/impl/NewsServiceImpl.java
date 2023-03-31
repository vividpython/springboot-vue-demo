package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.DrawingQueryParam;
import com.example.demo.common.NewsQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.News;
import com.example.demo.mapper.NewsMapper;
import com.example.demo.service.NewsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service("NewsService")
@Transactional
public class NewsServiceImpl extends ServiceImpl<NewsMapper,News> implements NewsService {
    //插入用户
    @Override
    public Result<?> insertNews(News news) {
        if (news == null ) {
            return Result.error("201", "参数错误");
        }
        news.setTime(new Date());
            // 执行插入数据操作
        return this.baseMapper.insert(news) == 0 ? Result.error("201","添加用户失败") : Result.success();

    }
    //查询列表
    @Override
    public Result<?> findNewsList(Integer index, Integer size, NewsQueryParam newsQueryParam) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }
        //}else if (size > 10) {
        //    return Result.error().message("一次最多10条数据");
        //}

        // 构建分页对象
        Page<News> page = new Page<>(index, size);
        // 查询
        IPage<News> iPage = this.baseMapper.findNewsList(page, newsQueryParam);

        // 回传两个数据, 一个 newsList --> 用户数据列表, 一个 total -> 总页数
        //return Result.success().data("newsList", iPage.getRecords()).data("total", iPage.getTotal());
        Result result = new Result<>(iPage);
        return Result.success(iPage);
    }
    //修改
    @Override
    public Result<?> modifyNews(News news) {
        if (news == null || news.getId() == null || news.getId() <= 0) return Result.error("201","参数错误");
        news.setTime(new Date());
        return this.baseMapper.updateById(news) == 0 ? Result.error("201","编辑用户失败") : Result.success();
    }
    //删除
    @Override
    public Result<?> deleteNews(Integer id) {
        if (id == null || id <= 0) return Result.error("201","参数错误");

        return  this.baseMapper.deleteById(id) == 0 ? Result.error("201","删除失败"): Result.success();
    }




}
