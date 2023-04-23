package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.DrawingQueryParam;
import com.example.demo.entity.Document;
import com.example.demo.entity.Drawing;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentMapper extends BaseMapper<Document> {
    /**
     * 查询用户列表
     * @param page 分页对象
     * @param drawingQueryParam 筛选条件
     * @return
     */
    IPage<Document> findDocumentList(Page<Document> page, DocumentQueryParam documentQueryParam);
    List<Document> findDocumentListByPN(String product_no);
    IPage<Document> findHistoryList(Page<Document> page, Document document);

    List<Document> findDocumentMore(@Param("ids") List<Integer> ids);

}
