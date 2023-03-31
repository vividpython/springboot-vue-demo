package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.UserQueryParam;
import com.example.demo.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
    /**
     * 查询用户列表
     * @param page 分页对象
     * @param userQueryParam 筛选条件
     * @return
     */
    IPage<User> findUserList(Page<User> page, UserQueryParam userQueryParam);
}
