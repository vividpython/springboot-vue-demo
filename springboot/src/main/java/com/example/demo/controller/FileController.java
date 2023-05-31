package com.example.demo.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;
import com.example.demo.entity.Drawing;
import com.example.demo.service.DocumentService;
import com.example.demo.service.DrawingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping("/files")
public class FileController {
    @Value("${server.port}")
    private String port;

    @Value("${nginx.port}")
    private String nginx_port;
    @Value("${nginx.location}")
    private String nginx_location;

    @Value("${file.ip}")
    private  String ip ;

    @Value("${file.location}")
    private  String file_location ;

    @Resource
    DrawingService drawingService;
    @Resource
    DocumentService documentService;
    @PostMapping("/uploadUserIcons")
    public Result<?> uploadUserIcons(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        //定义文件的唯一标识
        String flag = IdUtil.fastSimpleUUID();
        //String rootFilePath = System.getProperty("user.dir") + "/springboot/src/main/resources/userIcons/" + flag + "_" + originalFilename;
        //String rootFilePath ="/files/" + flag + "_" + originalFilename;
        String rootFilePath =file_location + flag + "_" + originalFilename;

        FileUtil.writeBytes(file.getBytes(),rootFilePath);
        //return Result.success("http://" +ip + ":" + port + "/files/" + flag + "_" + originalFilename);
        return Result.success("http://" + ip + ":" + nginx_port + "/" + flag + "_" + originalFilename);
    }
    //富文本图片上传处理函数
    @PostMapping("/editorUpload")
    public JSON editorUpload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        //定义文件的唯一标识
        String flag = IdUtil.fastSimpleUUID();
        //String rootFilePath = System.getProperty("user.dir") + "/springboot/src/main/resources/userIcons/" + flag + "_" + originalFilename;
        //String rootFilePath ="/files/" + flag + "_" + originalFilename;
        String rootFilePath =file_location + flag + "_" + originalFilename;

        FileUtil.writeBytes(file.getBytes(),rootFilePath);
        //return Result.success("http://" +ip + ":" + port + "/files/" + flag + "_" + originalFilename);
        String url = "http://" + ip + ":" + nginx_port + "/" + flag + "_" + originalFilename;
        JSONObject json = new JSONObject();
        json.set("errno",0);
        JSONArray arr = new JSONArray();
        JSONObject data = new JSONObject();
        arr.add(data);
        data.set("url",url);
        json.set("data",arr);
        return json;

        //return Result.success("http://" + ip + ":" + nginx_port + "/" + flag + "_" + originalFilename);
    }
    //图纸文件版本更新
    @PostMapping("/updateDrawingFiles")
    public Result<?> updateDrawingFiles(@RequestParam("file") MultipartFile file,@RequestParam("drawing") String drawingStr) throws IOException {
        String originalFilename = file.getOriginalFilename();
        System.out.println(drawingStr);
        String unescape = HtmlUtil.unescape(drawingStr);//还原成json字符串
        Drawing drawing = JSONUtil.toBean(unescape, Drawing.class);//Json转对象
        System.out.println(drawing);


        // 获取 drawingEntity 中的字段值
        Integer id = drawing.getId();

        String itemNo = drawing.getItemNo();
        String productNo = drawing.getProductNo();
        Integer drawingType = drawing.getDrawingType();
        String drawingTypeName = "";
        // 根据类型值获取对应的汉字
        Map<Integer, String> drawingTypeMap = new HashMap<>();
        drawingTypeMap.put(1, "材料清单");
        drawingTypeMap.put(2, "定屏图纸");
        drawingTypeMap.put(3, "配线图");
        drawingTypeMap.put(4, "技术交底单");
        if (drawingTypeMap.containsKey(drawingType)) {
            drawingTypeName = drawingTypeMap.get(drawingType);
        }
        // 根据信息创建对应的文件夹
        // 拼接文件夹路径
        //String folderPath = "/files/";
        String folderPath = file_location;
        //判断一下项目号是否为空 如果为空 则表示标品 如果不为空则表示项目需要特殊产品
        if (StringUtils.isNotBlank(itemNo)) {
            //判断一下有没有项目号  有项目号创建项目号路径
            folderPath +=  itemNo + "/" + productNo ;
        } else {
            folderPath += "StandardProduct/"  + productNo;
        }
        // 保存文件
        String NowDrawingVersion =  drawingService.getNowDrawingVersion(id);
        String NewDrawingVersion = "A" + String.format("%02d", Integer.parseInt(NowDrawingVersion.substring(1)) + 1);
        String filePath = folderPath + "/" +originalFilename.substring(0, originalFilename.lastIndexOf("."))
                + NewDrawingVersion + originalFilename.substring(originalFilename.lastIndexOf("."));
        FileUtil.writeBytes(file.getBytes(), filePath);

        // 构造返回数据
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("filePath", ip + ":" + nginx_port + filePath);
        resultMap.put("newVersion", NewDrawingVersion);

        return Result.success(resultMap);

        // 保存文件
        //定义文件的唯一标识
        //String flag = IdUtil.fastSimpleUUID();
        //String filePath = folderPath + "/" + flag + "_" + originalFilename;
        //FileUtil.writeBytes(file.getBytes(), filePath);
        //return Result.success(ip + "/" + folderPath + "/"  + flag + "_" + originalFilename );

        //定义文件的唯一标识
        //String flag = IdUtil.fastSimpleUUID();
        //String rootFilePath ="/files/" + flag + "_" + originalFilename;
        //FileUtil.writeBytes(file.getBytes(), rootFilePath);
        //return Result.success(ip + "/"  + flag + "_" + originalFilename );
    }
    @PostMapping("/updateDocumentFiles")
    public Result<?> updateDocumentFiles(@RequestParam("file") MultipartFile file,@RequestParam("document") String documentStr) throws IOException {
        String originalFilename = file.getOriginalFilename();
        System.out.println(documentStr);
        String unescape = HtmlUtil.unescape(documentStr);//还原成json字符串
        Document document = JSONUtil.toBean(unescape, Document.class);//Json转对象
        System.out.println(document);


        // 获取 drawingEntity 中的字段值
        Integer id = document.getId();
        String itemNo = document.getItemNo();
        String materialNo = document.getMaterialNo();
        Integer documentType = document.getDocumentType();
        String documentTypeName = "";
        // 根据类型值获取对应的汉字
        Map<Integer, String> documentTypeMap = new HashMap<>();
        documentTypeMap.put(1, "BOM");
        documentTypeMap.put(2, "APD");
        documentTypeMap.put(3, "EWD");
        documentTypeMap.put(4, "CN");
        documentTypeMap.put(5, "TDF");
        if (documentTypeMap.containsKey(documentType)) {
            documentTypeName = documentTypeMap.get(documentType);
        }
        // 根据信息创建对应的文件夹
        //String folderPath = "/files/";
        String folderPath = file_location;
        //判断一下料号是否为空 如果为空 则表示为备产产品 如果不为空则表示标准生产订单产品
        if (StringUtils.isNotBlank(materialNo)) {
            //判断一下有没有项目号  有项目号创建项目号路径
            folderPath += itemNo + "/" + materialNo + "/" +  documentTypeName;
        } else {
            folderPath += "itemNo/"  + documentTypeName;
        }
        // 保存文件
        String NowDocumentVersion =  documentService.getNowDocumentVersion(id);
        String NewDocumentVersion = "A" + String.format("%02d", Integer.parseInt(NowDocumentVersion.substring(1)) + 1);
        String filePath = folderPath + "/" +originalFilename.substring(0, originalFilename.lastIndexOf("."))
                + NewDocumentVersion + originalFilename.substring(originalFilename.lastIndexOf("."));
        FileUtil.writeBytes(file.getBytes(), filePath);

        // 构造返回数据
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("filePath", ip + ":" + nginx_port + filePath);
        resultMap.put("newVersion", NewDocumentVersion);

        return Result.success(resultMap);
    }

    @PostMapping("/uploadDrawingFiles")
    public Result<?> uploadDrawingFiles(@RequestParam("file") MultipartFile file,@RequestParam("drawing") String drawingStr) throws IOException {
        String originalFilename = file.getOriginalFilename();
        System.out.println(drawingStr);
        String unescape = HtmlUtil.unescape(drawingStr);//还原成json字符串
        Drawing drawing = JSONUtil.toBean(unescape, Drawing.class);//Json转对象
        System.out.println(drawing);


        // 获取 drawingEntity 中的字段值
        String itemNo = drawing.getItemNo();
        String productNo = drawing.getProductNo();
        Integer drawingType = drawing.getDrawingType();
        //String drawingTypeName = "";
        //// 根据类型值获取对应的汉字
        //Map<Integer, String> drawingTypeMap = new HashMap<>();
        //drawingTypeMap.put(1, "材料清单");
        //drawingTypeMap.put(2, "定屏图纸");
        //drawingTypeMap.put(3, "配线图");
        //drawingTypeMap.put(4, "技术交底单");
        //if (drawingTypeMap.containsKey(drawingType)) {
        //    drawingTypeName = drawingTypeMap.get(drawingType);
        //}
        // 根据信息创建对应的文件夹
        // 拼接文件夹路径
        //String folderPath = "/files/";
        String folderPath = file_location;
        //判断一下项目号是否为空 如果为空 则表示标品 如果不为空则表示项目需要特殊产品
        if (StringUtils.isNotBlank(itemNo)) {
            //判断一下有没有项目号  有项目号创建项目号路径
            folderPath +=  itemNo + "/" + productNo ;
        } else {
            folderPath += "StandardProduct/"  + productNo;
        }
        // 保存文件

        String filePath = folderPath + "/" +originalFilename.substring(0, originalFilename.lastIndexOf("."))
                + "A01" + originalFilename.substring(originalFilename.lastIndexOf("."));
        System.out.println("filePath:"+filePath);
        FileUtil.writeBytes(file.getBytes(), filePath);
        return Result.success(ip + ":" + nginx_port + filePath );
        // 保存文件
        //定义文件的唯一标识
        //String flag = IdUtil.fastSimpleUUID();
        //String filePath = folderPath + "/" + flag + "_" + originalFilename;
        //FileUtil.writeBytes(file.getBytes(), filePath);
        //return Result.success(ip + "/" + folderPath + "/"  + flag + "_" + originalFilename );

        //定义文件的唯一标识
        //String flag = IdUtil.fastSimpleUUID();
        //String rootFilePath ="/files/" + flag + "_" + originalFilename;
        //FileUtil.writeBytes(file.getBytes(), rootFilePath);
        //return Result.success(ip + "/"  + flag + "_" + originalFilename );
    }
    @PostMapping("/uploadDocumentFiles")
    public Result<?> uploadDocumentFiles(@RequestParam("file") MultipartFile file,@RequestParam("document") String documentStr) throws IOException {
        String originalFilename = file.getOriginalFilename();
        System.out.println(documentStr);
        String unescape = HtmlUtil.unescape(documentStr);//还原成json字符串
        Document document = JSONUtil.toBean(unescape, Document.class);//Json转对象
        System.out.println(document);


        // 获取 drawingEntity 中的字段值
        String itemNo = document.getItemNo();
        String materialNo = document.getMaterialNo();
        Integer documentType = document.getDocumentType();
        String documentTypeName = "";
        // 根据类型值获取对应的汉字
        Map<Integer, String> documentTypeMap = new HashMap<>();
        documentTypeMap.put(1, "BOM");
        documentTypeMap.put(2, "APD");
        documentTypeMap.put(3, "EWD");
        documentTypeMap.put(4, "CN");
        documentTypeMap.put(5, "TDF");
        if (documentTypeMap.containsKey(documentType)) {
            documentTypeName = documentTypeMap.get(documentType);
        }
         //根据信息创建对应的文件夹
         //拼接文件夹路径
        //String folderPath = "/files/";
        String folderPath = file_location;
        //判断一下料号是否为空 如果为空 则表示为备产产品 如果不为空则表示标准生产订单产品
        if (StringUtils.isNotBlank(materialNo)) {
            //判断一下有没有项目号  有项目号创建项目号路径
            folderPath +=   itemNo + "/" + materialNo + "/" +  documentTypeName;
        } else {
            folderPath += itemNo + "/"  + documentTypeName;
        }
        // 保存文件

        String filePath = folderPath + "/" +originalFilename.substring(0, originalFilename.lastIndexOf("."))
                + "A01" + originalFilename.substring(originalFilename.lastIndexOf("."));
        System.out.println("filePath:"+filePath);
        FileUtil.writeBytes(file.getBytes(), filePath);
        return Result.success(ip  + ":" + nginx_port + filePath );
        // 保存文件
        //定义文件的唯一标识
        //String flag = IdUtil.fastSimpleUUID();
        //String filePath = folderPath + "/" + flag + "_" + originalFilename;
        //FileUtil.writeBytes(file.getBytes(), filePath);
        //return Result.success(ip + "/" + folderPath + "/"  + flag + "_" + originalFilename );

        //定义文件的唯一标识
        //String flag = IdUtil.fastSimpleUUID();
        //String rootFilePath ="/files/" + flag + "_" + originalFilename;
        //FileUtil.writeBytes(file.getBytes(), rootFilePath);
        //return Result.success(ip + "/"  + flag + "_" + originalFilename );
    }

    @PostMapping("/delDrawingFile")
    public Result<?> delDrawingFile(@RequestBody String delDrawingPath){

        String delDrawingPath1 = delDrawingPath.replace("{\"delDrawingPath\":\"","").replace("\"}","");
        //String filePath = nginx_location
        //        + delDrawingPath1.substring(delDrawingPath1.indexOf("/files/") + 6);
        String filePath = nginx_location
                + delDrawingPath1.substring(delDrawingPath1.indexOf(file_location) + file_location.length()-1);
        System.out.println(filePath);
        File fileToDelete = new File(filePath);
        if (fileToDelete.delete()) {
            System.out.println("文件删除成功");
            return Result.success("文件删除成功！");
        } else {
            System.out.println("文件删除失败");
            return Result.error("201","文件删除失败！");
        }
    }
    //批量删除文件
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delDrawingFiles")
    public Result<?> delDrawingFiles(@RequestBody List<Integer> ids){
        List<Drawing> drawingMore = drawingService.findDrawingMore(ids);

        boolean flag = true; // 标记是否全部删除成功

        for (Drawing drawing : drawingMore) {
            String filePath = drawing.getDrawingPath();
            File fileToDelete = new File(filePath);
            if (!fileToDelete.delete()) {
                flag = false;
                break;
            }
        }
        if (flag) {
            return Result.success("文件删除成功！");
        } else {
            // 删除失败，事务回滚
            throw new RuntimeException("删除文件失败！");
        }
    }


    @PostMapping("/delDocumentFile")
    public Result<?> delDocumentFile(@RequestBody String delDocumentPath){
        String delDocumentPath1 = delDocumentPath.replace("{\"delDocumentPath\":\"","").replace("\"}","");
        //String filePath = nginx_location
        //        + delDocumentPath1.substring(delDocumentPath1.indexOf("/files/") + 6);
        String filePath = nginx_location
                + delDocumentPath1.substring(delDocumentPath1.indexOf(file_location) + file_location.length()-1);
        System.out.println("filePath"+filePath);
        File fileToDelete = new File(filePath);
        if (fileToDelete.delete()) {
            System.out.println("文件删除成功");
            return Result.success("文件删除成功！");
        } else {
            System.out.println("文件删除失败");
            return Result.error("201","文件删除失败！");
        }
    }
    //批量删除文件
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delDocumentFiles")
    public Result<?> delDocumentFiles(@RequestBody List<Integer> ids){
        List<Document> delDocumentMore = documentService.findDocumentMore(ids);

        boolean flag = true; // 标记是否全部删除成功

        for (Document document : delDocumentMore) {
            String filePath = document.getDocumentPath();
            File fileToDelete = new File(filePath);
            if (!fileToDelete.delete()) {
                flag = false;
                break;
            }
        }
        if (flag) {
            return Result.success("文件删除成功！");
        } else {
            // 删除失败，事务回滚
            throw new RuntimeException("删除文件失败！");
        }
    }



    //@GetMapping("/{flag}")
    public void getFiles(@PathVariable String flag, HttpServletResponse response){
        OutputStream os;
        String basePath = System.getProperty("user.dir") + "/springboot/src/main/resources/userIcons/";
        List<String> fileNames = FileUtil.listFileNames(basePath);
        String filename = fileNames.stream().filter(name -> name.contains(flag)).findAny().orElse("");
        try{
            if (StrUtil.isNotEmpty(filename)){
                response.addHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(filename,"UTF-8"));
                response.setContentType("application/octet-stream");
                byte[] bytes = FileUtil.readBytes(basePath  + filename);
                os = response.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();
            }
        }catch (Exception e){
            System.out.println("文件下载失败");
        }
    }




}
