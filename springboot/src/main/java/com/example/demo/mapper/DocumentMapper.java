package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.DrawingQueryParam;
import com.example.demo.entity.Document;
import com.example.demo.entity.Drawing;
import com.example.demo.entity.TypeDistributionData;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.print.Doc;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface DocumentMapper extends BaseMapper<Document> {
    /**
     * 查询用户列表
     * @param page 分页对象
     * @param documentQueryParam 筛选条件
     * @return
     */
    IPage<Document> findDocumentList(Page<Document> page, DocumentQueryParam documentQueryParam);

    IPage<Document> findHistoryList(Page<Document> page, Document document);

    List<Document> findDocumentMore(@Param("ids") List<Integer> ids);

    List<Document> findDocumentListByINTE(String itemNo, List<Integer> documentTypeList);

    List<Document> findDocumentListByLikeINTE(String itemNo, List<Integer> documentTypeList);
    List<Document> findDocumentToPublish();
    Integer departConfirm(Document document1);
    Integer selectByOneDate(Date date,Integer departId);
    Integer selectPublishByOneDate(Date date,Integer departId);

    List<TypeDistributionData> findtypeDistributionDataList();
    Document selectOneLast(Document document);

}
