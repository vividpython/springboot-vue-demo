package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.ItemMasterQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;
import com.example.demo.entity.ItemMaster;

import java.util.List;

public interface ItemMasterService extends IService<ItemMaster> {
    /**
     * 分页查询图纸列表
     * @param index 当前页
     * @param size 每页显示数量
     * @return
     */
    Result findItemMasterList(Integer index, Integer size,ItemMasterQueryParam itemMasterQueryParam);
    ItemMaster getItemMasterById(Long id);
}
