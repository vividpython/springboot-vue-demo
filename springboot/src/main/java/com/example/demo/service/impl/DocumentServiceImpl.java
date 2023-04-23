package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.controller.FileController;
import com.example.demo.entity.Document;
import com.example.demo.mapper.DocumentMapper;
import com.example.demo.service.DocumentService;
import com.example.demo.service.DrawingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("documentService")
@Transactional
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper,Document> implements DocumentService {

    //插入用户


    @Override
    public Result<?> insertDocument(Document document) {
        //插入数据的时候项目编号和文件类型不可以为空 备产的装置料号可以为空
        if (document == null || document.getItemNo() == null || document.getDocumentType() == null) {
            return Result.error("201", "参数错误");
        }
        int maxFileNumber = getMaxFileNumber(document.getItemNo(), document.getDocumentType());

        //如果料号为空 则为备产产品
        if (document.getMaterialNo() == null ){
            //备产产品
            document.setDocumentName(document.getItemNo() + document.getDocumentType() + "_" + maxFileNumber);
        }
        //如果是因为更新版本而插入数据 则要去掉带入的id
        if ( document.getId() != null ){
            document.setId(null);
        }
        if (document.getCreateTime() != null)
        {
            document.setCreateTime(null);
        }
        //项目编号
        String itemNo = document.getItemNo();
        // 产品料号
        String materialNo = document.getMaterialNo();
        // 图纸类型
        Integer documentType = document.getDocumentType();
        //图纸版本
        Integer sequenceNo = document.getSequenceNo();


        // 构建条件对象, 查询是否已经存在用户名
        QueryWrapper<Document> wrapper = new QueryWrapper<>();

        wrapper.select("id");
        wrapper.eq("item_no", itemNo);
        wrapper.eq("material_no", materialNo);
        wrapper.eq("document_type", documentType);
        wrapper.eq("sequence_no", sequenceNo);
        wrapper.last("limit 1");

        // 查询判断, 如果查询出来有数据, 则不为null
        if (this.baseMapper.selectOne(wrapper) != null)
        {
            return Result.error("201","该记录已存在");
        }
        else {
            // 执行插入数据操作
            return this.baseMapper.insert(document) == 0 ? Result.error("201","添加用户失败") : Result.success();
        }
    }
    //查询列表
    @Override
    public Result<?> findDocumentList(Integer index, Integer size, DocumentQueryParam documentQueryParam) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }
        //}else if (size > 10) {
        //    return Result.error().message("一次最多10条数据");
        //}

        // 构建分页对象
        Page<Document> page = new Page<>(index, size);
        // 查询
        IPage<Document> iPage = this.baseMapper.findDocumentList(page, documentQueryParam);

        // 回传两个数据, 一个 documentList --> 用户数据列表, 一个 total -> 总页数
        //return Result.success().data("documentList", iPage.getRecords()).data("total", iPage.getTotal());
        Result result = new Result<>(iPage);
        return Result.success(iPage);
    }
    //修改

    //删除单个用户
    @Override
    public Result<?> deleteDocument(Integer id) {
        if (id == null || id <= 0) return Result.error("201","参数错误");

        return  this.baseMapper.deleteById(id) == 0 ? Result.error("201","删除失败"): Result.success();
    }
    /**
     * @description: 按id查询数据
     * @param id: 
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/4/23 11:20
     */
    
    @Override
    public Result<?> getDocumentById(Integer id) {
        if (id == null || id <= 0)
            return Result.error("202","参数错误");

        return Result.success(this.baseMapper.selectById(id));
    }
    
    
    /**
     * @description: 查询文件的历史版本
     * @param index: 
 * @param size: 
 * @param document: 
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/4/23 11:22
     */
    
    @Override
    public Result<?> findHistoryList(Integer index, Integer size, Document document) {
        if (index == null || size == null || index <= 0 || size <= 0) {
            return Result.error("201", "参数错误");
        }

        // 构建分页对象
        Page<Document> page = new Page<>(index, size);
        // 查询
        IPage<Document> iPage = this.baseMapper.findHistoryList(page, document);

        Result result = new Result<>(iPage);
        return Result.success(iPage);
    }
    /**
     * @description: 按ids删除多个文件
     * @param ids: 
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/4/23 11:22
     */
    
    @Override
    public Result<?> deleteDocumentMore(List<Integer> ids) {
        if (ids.size() == 0) return Result.error("201","参数错误");
        return this.baseMapper.deleteBatchIds(ids) != ids.size() ? Result.error("201","删除失败") : Result.success();
    }
    /**
     * @description: 按照ids查询文件列表
     * @param ids: 
     * @return java.util.List<com.example.demo.entity.Document>
     * @author: Zheng
     * @date: 2023/4/23 11:23
     */
    
    @Override
    public List<Document> findDocumentMore(List<Integer> ids) {
        List<Document> documents = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            documents = this.baseMapper.findDocumentMore(ids);
        }
        return documents;
    }

    @Override
    public Integer getMaxFileNumber(String projectId, Integer fileType) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId)
                .eq("file_type", fileType)
                .likeRight("file_name", fileType + "-");

        Document document = this.baseMapper.selectOne(wrapper.orderByDesc("file_name").last("limit 1"));
        if (document == null) {
            return 0;
        }
        String fileName = document.getDocumentName();
        String numberStr = fileName.substring(fileName.lastIndexOf("-") + 1);
        return Integer.parseInt(numberStr);
    }

    @Override
    public Result<?> getFilePath(String itemNo, String materialNo) {
        return null;
    }


}
