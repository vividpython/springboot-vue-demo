package com.example.demo.controller;

import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;
import com.example.demo.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/document")
public class DocumentController {

    private final static Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Resource
    DocumentService documentService;

    //新增图纸信息
    @PostMapping
    public Result<?> save(@RequestBody Document document) {

        return documentService.insertDocument(document);
    }

    // 删除单个图纸信息
    @DeleteMapping("{id}")
    public Result<?> deleteDocument(@PathVariable(value = "id") Integer id) {
        return documentService.deleteDocument(id);
    }
    // 删除多个图纸
    @PostMapping("/deleteBatch")
    public Result<?> deleteDocumentMore(@RequestBody List<Integer> ids) {
        return documentService.deleteDocumentMore(ids);
    }



    // 查询图纸信息列表
    @PostMapping("{index}/{size}")
    public Result<?> findDocumentList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) DocumentQueryParam documentQueryParam) {

        Result<?> result=  documentService.findDocumentList(index, size, documentQueryParam);
        System.out.println("this is pull test code...");
        return result;
    }
    //获取历史版本的文件列表
    @PostMapping("/historyList/{index}/{size}")
    public Result<?> findHistoryList(@PathVariable(value = "index") Integer index,
                                     @PathVariable(value = "size") Integer size,
                                     @RequestBody(required = true) Document document) {

        Result<?> result=  documentService.findHistoryList(index, size, document);
        System.out.println("this is pull test code...");
        return result;
    }



    // 根据用户编号查询用户信息
    @GetMapping("/getId/{id}")
    public Result<?> getDocumentInfo(@PathVariable(value = "id") Integer id) {
        return documentService.getDocumentById(id);
    }


    //根据料号查询
    @GetMapping("/getDocumentPath")
    public Result<?> getDocumentPath(@RequestParam String itemNo,@RequestParam List<Integer> documentTypeList) {
        Result<?> filePaths = documentService.getFilePath(itemNo,documentTypeList);
        return filePaths;
    }
}
