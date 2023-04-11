package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.Result;
import com.example.demo.entity.SysOperLog;

import java.util.Date;

public interface SysOperLogService extends IService<SysOperLog> {
    /**
     * 添加用户
     * @param
     * @return
     */

    Result insertSysLog(String title, Integer businessType, String uri, Integer status, String operParam, String errorMsg, Date operTime,String operCreator,Integer operId);
}
