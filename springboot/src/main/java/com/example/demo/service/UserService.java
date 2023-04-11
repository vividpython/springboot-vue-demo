package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.Result;
import com.example.demo.common.UserQueryParam;
import com.example.demo.entity.User;

public interface UserService extends IService<User> {
    /**
     * 添加用户
     * @param user
     * @return
     */
    Result insertUser(User user);
    /**
     * 分页查询用户列表
     * @param index 当前页
     * @param size 每页显示数量
     * @param userQueryParam 筛选条件对象
     * @return
     */
    Result findUserList(Integer index, Integer size, UserQueryParam userQueryParam);

    Result<?> modifyUser(User user);

    Result<?> deleteUser(Integer id);

    Result<?> getOneUser(User user);

    Result<?> getUserInfoById(Integer id);

    User getUserInfolog(Integer id);
}
