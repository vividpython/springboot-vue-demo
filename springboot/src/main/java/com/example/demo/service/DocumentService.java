package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;

import java.util.List;

public interface DocumentService extends IService<Document> {
    /**
     * 添加用户
     * @param Document
     * @return
     */
    Result insertDocument(Document Document);
    /**
     * 分页查询图纸列表
     * @param index 当前页
     * @param size 每页显示数量
     * @param documentQueryParam 筛选条件对象
     * @return
     */
    Result findDocumentList(Integer index, Integer size, DocumentQueryParam documentQueryParam);


    Result<?> deleteDocument(Integer id);

    Result<?> getDocumentById(Integer id);






    Result<?> findHistoryList(Integer index, Integer size, Document Document);

    Result<?> deleteDocumentMore(List<Integer> ids);


    List<Document> findDocumentMore(List<Integer> ids);


    Integer getMaxFileNumber(String projectId, Integer fileType);

    Result<?> getFilePath(String itemNo, String materialNo);
}
