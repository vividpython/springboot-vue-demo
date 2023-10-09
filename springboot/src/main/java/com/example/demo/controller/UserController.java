package com.example.demo.controller;

import cn.hutool.json.JSONObject;
import com.example.demo.common.*;
import com.example.demo.entity.Depart;
import com.example.demo.entity.Document;
import com.example.demo.entity.User;
import com.example.demo.service.DepartService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/user")

public class UserController {

    @Resource
    UserService userService;
    @Resource
    DepartService departService;

    //六一测试修改
    //@Resource
    //private JwtConfig jwtConfig;
    @Resource
    private JwtUtils jwtUtils;
    //新增用户
    //@RequiresRoles("superuser")
    @RequiresPermissions("userManager:insertUser")
    @PostMapping
    @MyLog(title = "新增用户", operParam = "#{user}", businessType = BusinessType.INSERT)
    public Result<?> save(@RequestBody User user) {
        return userService.insertUser(user);
    }


    // 删除单个用户
    //@RequiresRoles("superuser")
    @RequiresPermissions("userManager:deleteUser")
    @DeleteMapping("{id}")
    @MyLog(title = "删除用户", operParam = "#{id}", businessType = BusinessType.DELETE)
    public Result<?> deleteUser(@PathVariable(value = "id") Integer id) {
        return userService.deleteUser(id);
    }


    // 编辑用户
    @PutMapping("")
    @MyLog(title = "编辑用户", operParam = "#{user}", businessType = BusinessType.UPDATE)
    public Result<?> modifyUser(@RequestBody User user, HttpSession session) {
        //Integer userId = (Integer) session.getAttribute("userId");
        return userService.modifyUser(user);
    }
    // 查询用户列表
    //@RequiresRoles("superuser")
    @RequiresPermissions("userManager:list")
    @PostMapping("{index}/{size}")

    public Result<?> findUserList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) UserQueryParam userQueryParam) {

        Result<User> result=  userService.findUserList(index, size, userQueryParam);
        return result;
    }
    //修改用户的启用禁用状态
    @PostMapping("/updateUserstatus")
    public Result<?> updateUserstatus(@RequestBody User user) {
        return userService.updateUserstatus(user);

    }

    @PostMapping("/handleUsersStatus/{status}")
    public Result<?> handleUsersStatus(@PathVariable(value = "status") Integer status,
            @RequestBody List<Integer> ids) {

        //设置两个列表 一个列表用来存放删除失败的记录的id 一个列表用来存放删除成功的记录中文件存储路径
        List<Integer> failedIds = new ArrayList<>();
        List<String> successUsers = new ArrayList<>();


        int successCount = 0;

        //对传入的ids 进行循环
        for (Integer id : ids) {
            User userInfolog = userService.getUserInfolog(id);
            if (userInfolog != null) {
                if (userInfolog.getStatus() != null){
                    //如果操作人和文件的创建人一致 则删除文件并且把文件的的文件路径保存起来
                    successUsers.add(userInfolog.getUsername());
                    userInfolog.setStatus(status);
                    userService.updateUserstatus(userInfolog);
                    successCount++;

                }
            }else {
                //只有那些可以删除 但是仅仅因为用户和创建人不一致得时候才加入失败得列表
                failedIds.add(id);
            }

        }

        int failedCount = ids.size() - successCount;
        if (failedCount == 0) {
            //如果全部能全部删除成功了 也要把这些能删除成功的id对应的文件路径用列表返回
            //全部删除成功
            return Result.success("0","全部删除成功",successUsers);
        } else if (failedCount == ids.size()) {
            //全部删除失败
            String errorMessage = String.format("删除失败：%s", failedIds);
            return Result.error("201", errorMessage);
        } else {
            String Message = String.format("删除成功：%d 条记录,删除失败：%d条记录", successCount,failedCount);
            return Result.success("101",Message, successUsers);
        }
    }




    // 根据用户编号查询用户信息
    //@RequiresRoles(logical = Logical.OR, value = {"superuser","department_admin" ,"designer","regular_user"})
    @GetMapping("{id}")
    public Result<?> getUserInfoById(@PathVariable(value = "id") Integer id) {
        return userService.getUserInfoById(id);
    }


    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer","regular_user"})
    @PostMapping("/findListByName")
    public Result<?> findListByName(@RequestBody String userName,HttpServletRequest request) {


        //首先获取判断用户的角色 如果是普通用户则只能看到审批通过的图纸 其他用户则可以看到所有图纸信息
        //从携带的token中获取当前用户的信息
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"), "username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        //判断一下是不是能从token中获取用户信息
        if (userByUsername == null || userByUsername.getId() == null) {
            //如果获取用户信息失败 说明无法获取操作人的用户信息
            return Result.error("201", "获取用户参数失败");
        } else {
            //查询用户的角色
            String roleKey = userByUsername.getRole().getRoleKey();




            //验证操作用户所属的部门 如果用户所在的部门属于两个事业部 则应该先验证对应的事业部
            //如果操作用户所属的部门 不属于两个事业部 则只需要验证用户的身份就可以了
            Integer parentId = userByUsername.getDepart().getParentId();
            Depart departByparentId = departService.getDepartById(parentId);
            String departKey = departByparentId.getDepartKey();
            Integer departId = null;
            //如果查出来的用户的部门的父部门是物联网事业部或者新能源事业部 则需要把这个父id parentId设置到查询的条件类中
            if ("IOTB".equals(departKey) || "NETB".equals(departKey)) {
                departId = departByparentId.getId();
            }


            System.out.println("userName:" + userName.substring(1, userName.length() - 1));
            Result<?> result = userService.findListByName(userName.substring(1, userName.length() - 1),departId);
            System.out.println("this is pull test code...");
            return result;
        }
    }
    /**
     * 根据请求头的token获取userId
     *
     * @param request
     * @return
     */
    //@RequiresRoles(logical = Logical.OR, value = {"superuser","department_admin", "designer","regular_user"})
    @GetMapping("/getUserInfo")
    //public Result<?> getUserInfo(HttpServletRequest request) {
    //    String usernameFromToken = jwtConfig.getUsernameFromToken(request.getHeader("token"));
    //    return Result.success(usernameFromToken);
    //}
    public Result<?> getUserInfo(HttpServletRequest request) {
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        return Result.success(userByUsername.getId());
    }

    /**
     * 重置密码
     *
     * @param
     * @return
     */
    //@RequiresRoles(logical = Logical.OR, value = {"superuser","designer","regular_user"})
    @GetMapping("/resetPassword/{id}")
    public Result<?> resetPassword(@PathVariable(value = "id") Integer id) {
        return Result.success(userService.resetPassword(id));
    }


    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        return userService.insertUser(user);
    }

    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer","regular_user"})
    @PostMapping("/confirmPassword")
    public Result<?> confirmPassword(@RequestBody User user) {
        return userService.confirmPassword(user);
    }


}
