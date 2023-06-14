package com.example.demo.controller;

import cn.hutool.core.collection.CollUtil;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;
import com.example.demo.entity.User;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/document")
public class DocumentController {

    private final static Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    DocumentService documentService;

    @Autowired
    UserService userService;

    //新增图纸信息
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
    @PostMapping
    public Result<?> save(@RequestBody Document document) {
        return documentService.insertDocument(document);
    }
    //批量新增
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
    @PostMapping("/insert")
    public Result<?> save1(@RequestBody Document document) {

        return documentService.insertDocument1(document);
    }
    /**
     * @description: 此函数用在验证批量导入的时候文件是否重复的问题
     * @param document:
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/6/7 15:11
     */

    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
    @PostMapping("/confirm")
    public Result<?> confirm(@RequestBody Document document) {

        return documentService.confirmDocument(document);
    }

    //更新时候的用户验证
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
    @PostMapping("/updateconfirm")
    public Result<?> updateconfirm(@RequestBody Document document,HttpServletRequest request) {

        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        Integer userId = document.getUserId();
        //判断一下是不是能从token中获取用户信息
        if (userByUsername == null || userByUsername.getId() == null ){
            //如果获取用户信息失败 说明无法获取操作人的用户信息
            return Result.error("201", "获取用户参数失败");
        }
        if (userByUsername.getId().equals(userId)) {
            return Result.success();
        } else {
            return Result.error("201", "非本人资源,禁止更新操作");
        }
    }

    // 删除单个图纸信息
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
    @DeleteMapping("{id}")
    public Result<?> deleteDocument(@PathVariable(value = "id") Integer id, HttpServletRequest request) {
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        Result<?> documentById = documentService.getDocumentById(id);
        Object data = documentById.getData();
        if (data instanceof Document) {
            Document document = (Document) data;
            Integer userId = document.getUserId();
            if (userByUsername != null && userByUsername.getId() != null && userByUsername.getId().equals(userId)) {
                return documentService.deleteDocument(id);
            } else {
                return Result.error("201", "非本人资源,禁止删除");
            }
        } else {
            // handle error here...
            return Result.error("201", "系统出错 删除失败");
        }
    }
    // 删除多个图纸
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
    @PostMapping("/deleteBatch")
    public Result<?> deleteDocumentMore(@RequestBody List<Integer> ids,HttpServletRequest request) {
        //从携带的token中获取当前用户的信息
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        //判断一下是不是能从token中获取用户信息
        if (userByUsername == null || userByUsername.getId() == null ){
            //如果获取用户信息失败 说明无法获取操作人的用户信息
            return Result.error("201", "获取用户参数失败");
        }
        //设置两个列表 一个列表用来存放删除失败的记录的id 一个列表用来存放删除成功的记录中文件存储路径
        List<Integer> failedIds = new ArrayList<>();
        List<String> successPaths = new ArrayList<>();


        int successCount = 0;

        //对传入的ids 进行循环
        for (Integer id : ids) {
            Result<?> documentById = documentService.getDocumentById(id);
            Object data = documentById.getData();
            if (data instanceof Document document) {
                if (userByUsername != null && document.getUserId().equals(userByUsername.getId())){
                    //如果操作人和文件的创建人一致 则删除文件并且把文件的的文件路径保存起来
                    successPaths.add(document.getDocumentPath());
                    Result<?> result = documentService.deleteDocument(id);
                    successCount++;

                }else{
                    //只有那些可以删除 但是仅仅因为用户和创建人不一致得时候才加入失败得列表
                    failedIds.add(id);
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
            return Result.success("0","全部删除成功",successPaths);
        } else if (failedCount == ids.size()) {
            //全部删除失败
            String errorMessage = String.format("删除失败：%s", failedIds.toString());
            return Result.error("201", errorMessage);
        } else {
            String Message = String.format("删除成功：%d 条记录,删除失败：%d条记录", successCount,failedCount);
            return Result.success("101",Message, successPaths);
        }
    }



    // 查询图纸信息列表
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer","worker"})
    @PostMapping("{index}/{size}")
    public Result<?> findDocumentList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) DocumentQueryParam documentQueryParam) {

        Result<?> result=  documentService.findDocumentList(index, size, documentQueryParam);
        System.out.println("this is pull test code...");
        return result;
    }
    //获取历史版本的文件列表
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer","worker"})
    @PostMapping("/historyList/{index}/{size}")
    public Result<?> findHistoryList(@PathVariable(value = "index") Integer index,
                                     @PathVariable(value = "size") Integer size,
                                     @RequestBody(required = true) Document document) {

        Result<?> result=  documentService.findHistoryList(index, size, document);
        System.out.println("this is pull test code...");
        return result;
    }



    // 根据图纸标号编号查询用户信息 预览用
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer","worker"})
    @GetMapping("/getId/{id}")
    public Result<?> getDocumentInfo(@PathVariable(value = "id") Integer id) {
        return documentService.getDocumentById(id);
    }


    //根据料号查询 MES接口
    @GetMapping("/getDocumentPath")
    public Result<?> getDocumentPath(@RequestParam String itemNo,@RequestParam List<Integer> documentTypeList) {
        Result<?> filePaths = documentService.getFilePath(itemNo,documentTypeList);
        return filePaths;
    }
}
