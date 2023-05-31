package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.ItemMasterQueryParam;
import com.example.demo.entity.Document;
import com.example.demo.entity.ItemMaster;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemMasterMapper extends BaseMapper<ItemMaster> {
    IPage<ItemMaster> findItemMasterList(Page<ItemMaster> page,ItemMasterQueryParam itemMasterQueryParam);

}
