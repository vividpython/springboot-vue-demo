package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.common.UserQueryParam;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;

@Service("userService")
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {
    //插入用户
    @Override
    public Result<?> insertUser(User user) {
        if (user == null) return Result.error("201","参数错误");
        if (user.getPassword() == null) {
            user.setPassword("123456");
        }

        // 用户名
        String username = user.getUsername();

        // 构建条件对象, 查询是否已经存在用户名
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("username", username);
        wrapper.last("limit 1");

        // 查询判断, 如果查询出来有数据, 则不为null
        if (this.baseMapper.selectOne(wrapper) != null) Result.error("201","该用户名已存在");

        // 执行插入数据操作
        return this.baseMapper.insert(user) == 0 ? Result.error("201","添加用户失败") : Result.success();
    }
    //查询用户列表
    @Override
    public Result<?> findUserList(Integer index, Integer size, UserQueryParam userQueryParam) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }
        //}else if (size > 10) {
        //    return Result.error().message("一次最多10条数据");
        //}



        // 构建分页对象
        Page<User> page = new Page<>(index, size);
        // 查询
        IPage<User> iPage = this.baseMapper.findUserList(page, userQueryParam);

        // 回传两个数据, 一个 userList --> 用户数据列表, 一个 total -> 总页数
        //return Result.success().data("userList", iPage.getRecords()).data("total", iPage.getTotal());
        Result result = new Result<>(iPage);
        return Result.success(iPage);
    }
    //修改用户
    @Override
    public Result<?> modifyUser(User user) {
        if (user == null || user.getId() == null || user.getId() <= 0) return Result.error("201","参数错误");

        return this.baseMapper.updateById(user) == 0 ? Result.error("201","编辑用户失败") : Result.success();
    }
    //删除单个用户
    @Override
    public Result<?> deleteUser(Integer id) {
        if (id == null || id <= 0) return Result.error("201","参数错误");

        return  this.baseMapper.deleteById(id) == 0 ? Result.error("201","删除失败"): Result.success();
    }

    @Override
    public Result<?> getOneUser(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Result.error("201", "参数错误");
        }
        // 用户名
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        wrapper.eq("password", user.getPassword());
        User res =  this.baseMapper.selectOne(wrapper);
        if (res == null){
            return Result.error("201","用户名或密码错误");
        }
        return Result.success(res);
    }

    @Override
    public Result<?> getUserInfoById(Integer id) {
            if (id == null || id <= 0) return Result.error("201","参数错误");
            return Result.success(this.baseMapper.selectById(id));
        }

    @Override
    public User getUserInfolog(Integer id) {
        if (id == null || id <= 0) return null;
        User res =  this.baseMapper.selectById(id);
        return res;
    }

}
