package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.common.DocumentQueryParam;
import com.example.demo.entity.Document;
import com.example.demo.entity.Drawing;
import com.example.demo.mapper.DocumentMapper;
import com.example.demo.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.print.Doc;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("documentService")
@Transactional
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper,Document> implements DocumentService {



    @Value("${nginx.location}")
    private String nginx_location;
    //插入用户

    @Override
    public Result<?> insertDocument(Document document) {
        //插入数据的时候项目编号和文件类型不可以为空 备产的装置料号可以为空
        if (document == null || document.getItemNo() == null || document.getDocumentType() == null) {
            return Result.error("201", "参数错误");
        }



        //获取当前文件的文件序号的最大值 分为两种情况 一种是备产的产品（不带料号） 一种是标准项目文件（带料号）


        String documentTypeValue = "";
        //// 根据类型值获取对应的汉字
        Map<Integer, String> documentTypeMap = new HashMap<>();
        documentTypeMap.put(1, "材料清单");
        documentTypeMap.put(2, "装配工艺图");
        documentTypeMap.put(3, "电气接线图");
        documentTypeMap.put(4, "变更通知单");
        documentTypeMap.put(5, "技术交底单");
        if (documentTypeMap.containsKey(document.getDocumentType())) {
            documentTypeValue = documentTypeMap.get(document.getDocumentType());
        }

        //判断一下是因为更新文件版本的插入数据还是第一次的数据插入
        //如果是因为更新版本而插入数据 则要去掉带入的id 并且文件名中的
        if ( document.getId() != null ){
            document.setId(null);
            document.setCreateTime(null);

            //如果料号为空 则为备产产品
            if (document.getMaterialNo() == null ){
                //备产产品
                document.setDocumentName(document.getItemNo() + "_" + documentTypeValue + "_" + document.getSequenceNo());
            }else{
                //如果是带料号的项目产品则 文件名为项目编号+料号+产品类型+序号
                document.setDocumentName(document.getItemNo() + "_" + document.getMaterialNo() + "_" + documentTypeValue + "_" + document.getSequenceNo());
            }

        }else{
            //如果是该文件的第一次存入 即版本A01
            //查询出当前文件序号的最大值 如果不存在文件则为0
            int maxFileNumber = getMaxFileNumber(document.getItemNo(),document.getMaterialNo(), document.getDocumentType()) + 1;
            document.setSequenceNo(maxFileNumber);
            //如果料号为空 则为备产产品
            if (document.getMaterialNo() == null ){
                //备产产品
                document.setDocumentName(document.getItemNo() + "_" + documentTypeValue + "_" + maxFileNumber);
            }else{
                //如果是带料号的项目产品则 文件名为项目编号+料号+产品类型+序号
                document.setDocumentName(document.getItemNo() + "_" + document.getMaterialNo() + "_" + documentTypeValue + "_" + maxFileNumber);
            }


        }




        //项目编号
        String itemNo = document.getItemNo();
        // 产品料号
        String materialNo = document.getMaterialNo();
        //文件版本
        String documentVersion = document.getDocumentVersion();

        // 图纸类型
        Integer documentType = document.getDocumentType();
        //图纸版本
        Integer sequenceNo = document.getSequenceNo();


        // 构建条件对象, 查询是否已经存在用户名
        QueryWrapper<Document> wrapper = new QueryWrapper<>();

        if (materialNo == null){
            wrapper.select("id");
            wrapper.eq("item_no", itemNo);
            wrapper.eq("document_type", documentType);
            wrapper.eq("sequence_no", sequenceNo);
            wrapper.eq("document_version", documentVersion);
            wrapper.last("limit 1");
        }else {
            wrapper.select("id");
            wrapper.eq("item_no", itemNo);
            wrapper.eq("material_no", materialNo);
            wrapper.eq("document_type", documentType);
            wrapper.eq("sequence_no", sequenceNo);
            wrapper.eq("document_version", documentVersion);
            wrapper.last("limit 1");
        }


        // 查询判断, 如果查询出来有数据, 则不为null
        if (this.baseMapper.selectOne(wrapper) != null)
        {
            return Result.error("201","该记录已存在");
        }
        else {
            //如果可以插入该条数据需要去文件系统中把文件名改掉 改成自动生成的文件名
            String documentName = document.getDocumentName();
            String documentPath = document.getDocumentPath();
            this.renameFile(documentName,documentPath);
            document.setDocumentPath(renameDocumentPath(documentName,documentPath));
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
        System.out.println("document:"+document);

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
    public Integer getMaxFileNumber(String itemNo, String materialNo, Integer documentType) {

        String documentTypeValue = "";
        //// 根据类型值获取对应的汉字
        Map<Integer, String> documentTypeMap = new HashMap<>();
        documentTypeMap.put(1, "材料清单");
        documentTypeMap.put(2, "装配工艺图");
        documentTypeMap.put(3, "电气接线图");
        documentTypeMap.put(4, "变更通知单");
        documentTypeMap.put(5, "技术交底单");
        if (documentTypeMap.containsKey(documentType)) {
            documentTypeValue = documentTypeMap.get(documentType);
        }
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        if(materialNo == null){

            wrapper.eq("item_no", itemNo)
                    .eq("document_type", documentType)
                    .likeRight("document_name", documentTypeValue + "-");
        }else {
            wrapper.eq("item_no", itemNo)
                    .eq("material_no", materialNo)
                    .eq("document_type", documentType)
                    .like("document_name", "%" + materialNo + "_" + documentTypeValue + "%");
        }



        Document document = this.baseMapper.selectOne(wrapper.orderByDesc("document_name").last("limit 1"));
        if (document == null) {
            return 0;
        }
        String fileName = document.getDocumentName();
        String numberStr = fileName.substring(fileName.lastIndexOf("_") + 1);
        return Integer.parseInt(numberStr);
    }
    @Override
    public Result<?> getFilePath(String itemNo, List<Integer> documentTypeList) {
        if (itemNo == null) {
            return Result.error("201", "参数错误");
        }
        if (documentTypeList == null){
            return Result.error("201", "参数错误");
        }
        // 用户名
        List<Document> documentListByINTE = this.baseMapper.findDocumentListByINTE(itemNo,documentTypeList);

        return Result.success(documentListByINTE);
        //return null;
    }

    @Override
    public String getNowDocumentVersion(Integer id) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.select("document_version").eq("id", id).orderByDesc("create_time").last("limit 1");
        Document document = this.baseMapper.selectOne(wrapper);
        if (document == null) {
            return null;
        }
        return document.getDocumentVersion();
    }

    public String renameDocumentPath(String documentName, String documentPath) {

        // 获取文件名
        String fileName = documentPath.substring(documentPath.lastIndexOf("/") + 1);
        // 获取文件所在文件夹的路径
        String folderPath = documentPath.substring(0, documentPath.lastIndexOf("/") + 1);
        // 获取文件后缀名
        String fileSuffix = fileName.substring(fileName.lastIndexOf(".") - 3);
        // 构造新的文件名
        String newFileName = folderPath + documentName + fileSuffix;

        return newFileName;

    }
    public void renameFile(String documentName, String documentPath) {

        String subPath = nginx_location +  documentPath.substring(documentPath.indexOf("files/") + 6);
        // 获取文件名
        String fileName = subPath.substring(subPath.lastIndexOf("/") + 1);
        // 获取文件所在文件夹的路径
        String folderPath = subPath.substring(0, subPath.lastIndexOf("/") + 1);
        // 获取文件后缀名
        String fileSuffix = fileName.substring(fileName.lastIndexOf(".") - 3);
        // 构造新的文件名
        String newFileName = folderPath + documentName + fileSuffix;



        // 创建文件对象
        File oldFile = new File(subPath);
        File newFile = new File(newFileName);
        // 重命名文件
        if (oldFile.renameTo(newFile)) {
            System.out.println("文件重命名成功！");
        } else {
            System.out.println("文件重命名失败！");
        }
    }
}
