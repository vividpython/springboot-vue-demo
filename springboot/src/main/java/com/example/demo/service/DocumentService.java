package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;
import com.example.demo.entity.User;

import java.util.List;

public interface DocumentService extends IService<Document> {
    /**
     * 添加文件（单选添加 带文件序号管理）
     * @param Document
     * @return
     */
    Result insertDocument(Document Document);
    /**
     * @description: 添加文件（多选添加 不带文件序号管理）
     * @param Document:
     * @return com.example.demo.common.Result
     * @author: Zheng
     * @date: 2023/6/8 9:07
     */

    Result insertDocument1(Document Document);

    /**
     * @description: 重复上传文件验证 （多选添加使用）
     * @param document:
     * @return com.example.demo.common.Result
     * @author: Zheng
     * @date: 2023/6/8 9:08
     */

    Result confirmDocument(Document document);

    /**
     * @description: 更新（设计变更）
     * @param document: 
     * @return com.example.demo.common.Result
     * @author: Zheng
     * @date: 2023/6/20 11:20
     */
    
    Result updateDocumentM(Document document);
    //更新图纸状态
    Result updatestatus(Document document);
    /**
     * 分页查询图纸列表
     * @param index 当前页
     * @param size 每页显示数量
     * @param documentQueryParam 筛选条件对象
     * @return
     */
    Result findDocumentList(Integer index, Integer size, DocumentQueryParam documentQueryParam);

    Document findDocumentCreateTime(Document document);

    Result<?> deleteDocument(Integer id);
    /**
     * @description: 根据id查询
     * @param id:
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/5/17 17:31
     */

    Result<?> getDocumentById(Integer id);



    Integer departConfirm( Document document);


    Result<?> findHistoryList(Integer index, Integer size, Document document);

    Result<?> deleteDocumentMore(List<Integer> ids);


    List<Document> findDocumentMore(List<Integer> ids);


    Integer getMaxFileNumber(String ItemNo, String materialNo, Integer documentType);

    Result<?> getFilePath(String itemNo, List<Integer> documentTypeList);

    String getNowDocumentVersion(Integer id);
}
