package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.common.SysOperLog;

import com.example.demo.mapper.SysLogMapper;

import com.example.demo.service.SysOperLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service("sysOperLogService")
@Transactional
public class SysOperLogServiceImpl extends ServiceImpl<SysLogMapper, SysOperLog> implements SysOperLogService {


    @Override
    public Result insertSysLog(String title, Integer businessType, String uri, Integer status, String optParam, String errorMsg, Date operTime) {

        SysOperLog sysOperLog = new SysOperLog()
                .setTitle(title)
                .setBusinessType(businessType)
                .setUri(uri)
                .setStatus(status)
                .setOptParam(optParam)
                .setErrorMsg(errorMsg)
                .setOperTime(operTime);
        return  this.baseMapper.insert(sysOperLog) == 0 ? Result.error("201","存入日志失败") : Result.success();
    }
}