package com.example.demo.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.ItemMasterQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;
import com.example.demo.entity.ItemMaster;
import com.example.demo.mapper.DocumentMapper;
import com.example.demo.mapper.ItemMasterMapper;
import com.example.demo.service.DocumentService;
import com.example.demo.service.ItemMasterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("itemMasterService")
@Transactional
@DS("slave")
public class ItemMasterServiceImpl extends ServiceImpl<ItemMasterMapper, ItemMaster> implements ItemMasterService {
    @Override
    public Result findItemMasterList(Integer index, Integer size,ItemMasterQueryParam itemMasterQueryParam) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }
        // 构建分页对象
        Page<ItemMaster> page = new Page<>(index, size);
        // 查询
        IPage<ItemMaster> iPage = this.baseMapper.findItemMasterList(page,itemMasterQueryParam);

        // 回传两个数据, 一个 documentList --> 用户数据列表, 一个 total -> 总页数
        //return Result.success().data("documentList", iPage.getRecords()).data("total", iPage.getTotal());
        return Result.success(iPage);
    }

    @Override
    public ItemMaster getItemMasterById(Long id) {
        if (id == null || id <= 0)
            return null;

        return this.baseMapper.selectById(id);
    }
}
