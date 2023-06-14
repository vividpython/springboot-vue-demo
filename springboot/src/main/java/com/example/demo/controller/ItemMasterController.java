package com.example.demo.controller;


import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.ItemMasterQueryParam;
import com.example.demo.common.Result;
import com.example.demo.service.ItemMasterService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itemMaster")
public class ItemMasterController {

    private final static Logger logger = LoggerFactory.getLogger(ItemMasterController.class);

    @Autowired
    ItemMasterService itemMasterService;



    // 查询图纸信息列表
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
    @PostMapping("{index}/{size}")
    public Result<?> findItemMasterList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) ItemMasterQueryParam itemMasterQueryParam) {

        Result<?> result=  itemMasterService.findItemMasterList(index, size,itemMasterQueryParam);
        System.out.println("this is pull test code...");
        return result;
    }

}
