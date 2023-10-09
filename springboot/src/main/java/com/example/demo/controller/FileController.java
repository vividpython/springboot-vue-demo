package com.example.demo.controller;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.demo.common.ExtractCallback;
import com.example.demo.common.Result;
import com.example.demo.entity.Document;
import com.example.demo.entity.Drawing;
import com.example.demo.service.DocumentService;
import com.example.demo.service.DrawingService;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


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


    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/uploadUserIcons")
    public Result<?> uploadUserIcons(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        //定义文件的唯一标识
        String flag = IdUtil.fastSimpleUUID();
        //String rootFilePath = System.getProperty("user.dir") + "/springboot/src/main/resources/userIcons/" + flag + "_" + originalFilename;
        //String rootFilePath ="/files/" + flag + "_" + originalFilename;
        String rootFilePath =file_location +"userIcons/"  + flag + "_" + originalFilename;

        FileUtil.writeBytes(file.getBytes(),rootFilePath);
        //return Result.success("http://" +ip + ":" + port + "/files/" + flag + "_" + originalFilename);
        return Result.success("http://" + ip + ":" + nginx_port + file_location +  "userIcons/" + flag + "_" + originalFilename);
    }
    //富文本图片上传处理函数
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
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
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
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
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
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
            folderPath += itemNo + "/"  + documentTypeName;
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


    /*
    *   //1 首先找到待更改的文件
        //2 判断待更改的文件是否为压缩包
        //  如果待更改的文件为压缩包进入下一步
        //  如果待更改的文件不是压缩包 则直接拒绝更改
        //3 解压原来的文件 并且根据文件名称核对 解压文件中是否有要更新的文件
        //  如果含有要更细的文件进入下一步
        //  如果不含有待更新的文件 则直接拒绝
        //4 复制原来的压缩包 并且起名加copy后缀 然后把待更新的文件放到copy压缩包中替代
        //5
    *
    * */
    @PostMapping("/editDocumentFiles")
    public Result<?> editDocumentFiles(@RequestParam("file") MultipartFile file,@RequestParam("document") String documentStr) throws IOException {
        String originalFilename = file.getOriginalFilename();
        System.out.println(documentStr);
        String unescape = HtmlUtil.unescape(documentStr);//还原成json字符串
        Document document = JSONUtil.toBean(unescape, Document.class);//Json转对象
        System.out.println(document);

        //声明返回变量
        String newZipFilePath = "";
        String NowDocumentVersion = "";
        String NewDocumentVersion = "";



        // 获取 drawingEntity 中的字段值
        Integer id = document.getId();
        String itemNo = document.getItemNo();
        String materialNo = document.getMaterialNo();
        Integer documentType = document.getDocumentType();
        String fileNameWithoutExtension = document.getDocumentName();
        String fileVersion = document.getDocumentVersion();
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
        // 首先找到待更改的文件 拼接出目标文件夹的路径
        //String folderPath = "/files/";
        String folderPath = file_location;
        //判断一下料号是否为空 如果为空 则表示为备产产品 如果不为空则表示标准生产订单产品
        if (StringUtils.isNotBlank(materialNo)) {
            //判断一下有没有项目号  有项目号创建项目号路径
            folderPath += itemNo + "/" + materialNo + "/" +  documentTypeName;
        } else {
            folderPath += itemNo + "/"  + documentTypeName;
        }

        // 获取文件夹下所有文件
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        // 遍历文件数组，判断是否存在匹配的文件名（不带后缀）
        if (files != null) {
            for (File onefile : files) {
                String fileName = onefile.getName();
                int dotIndex = fileName.lastIndexOf('.');
                String nameWithoutExtension = (dotIndex > 0 && dotIndex < fileName.length() - 1)
                        ? fileName.substring(0, dotIndex)
                        : fileName;

                if (nameWithoutExtension.equals(fileNameWithoutExtension + fileVersion)) {
                    // 匹配到相同文件名（不带后缀）的文件
                    System.out.println("文件存在");
                    //获取文件的后缀
                    String extension = getFileExtension(onefile.getName());
                    //如果文件的后缀不为空 即获取成功
                    if (extension != ""){
                        // 判断文件类型是否为压缩文件
                         if (extension.equals("rar") || extension.equals("zip")) {
                            //  如果是压缩文件 处理压缩文件的逻辑
                            // 首先核对压缩包中是否含有和上传的文件同名的文件
                            String zipFilePath = folderPath + "/" + fileName;
                            String targetFileName = file.getOriginalFilename();
                            boolean isFilePresent = isFileInZip(zipFilePath, targetFileName);

                            if (isFilePresent) {
                                // 复制压缩文件
                                String copiedZipFilePath = folderPath + "/copied_" + fileName;
                                copyFile(zipFilePath, copiedZipFilePath);

                                // 解压缩被复制的压缩文件
                                String extractedFolderPath = folderPath + "/" + fileNameWithoutExtension + "/extracted";
                                unzipFile(copiedZipFilePath, extractedFolderPath);

                                // 替换文件
                                //extractedFolderPath 解压之后的文件夹路径
                                //targetFileName 传入的文件的文件名
                                //replacementFilePath 传入的文件的文件路径
                                String replacementFilePath = folderPath + "/" + targetFileName;
                                FileUtil.writeBytes(file.getBytes(), replacementFilePath);
                                replaceFile(extractedFolderPath, targetFileName, replacementFilePath);

                                // 重新打包压缩文件 这个新的压缩文件就是替换完文件之后的压缩包 也就是新版本的文件
                                NowDocumentVersion =  documentService.getNowDocumentVersion(id);
                                NewDocumentVersion = "A" + String.format("%02d", Integer.parseInt(NowDocumentVersion.substring(1)) + 1);
                                // 获取不包含后缀的文件名
                                String baseName = fileName.substring(0, fileName.lastIndexOf("."));
                                //获取不包含版本信息的文件名
                                String desiredPart = baseName.substring(0, baseName.length() - 3);


                                //newZipFilePath = folderPath + "/" +desiredPart
                                //        + fileName.substring(fileName.lastIndexOf("."));
                                newZipFilePath = folderPath + "/" +desiredPart
                                        + ".zip";
                                zipFile(extractedFolderPath, newZipFilePath);

                                // 删除解压产生的文件夹
                                File extractedFolder = new File(folderPath + "/" + fileNameWithoutExtension);
                                deleteFolder(extractedFolder);

                                // 删除复制的压缩文件和传入的文件（在项目的根目录）
                                File copiedZipFile = new File(copiedZipFilePath);
                                File replacementFile = new File(replacementFilePath);
                                if (copiedZipFile.exists()) {
                                    copiedZipFile.delete();
                                }
                                if (replacementFile.exists()) {
                                    replacementFile.delete();
                                }
                                System.out.println("处理压缩文件的逻辑完成");








                            } else {
                                System.out.println("文件不存在或者无法访问");
                                return Result.error("201",  "源文件不存在或源文件无法访问");
                            }



                            System.out.println("文件是压缩文件");
                        } else {
                            //如果不是压缩文件
                            System.out.println("文件不是压缩文件");
                            return Result.error("202","被更新的文件不是压缩文件");

                        }
                    }else {
                        //如果获取文件的后缀名失败
                        return Result.error("203","文件后缀获取失败");
                    }



                    break;
                }
            }
        } else {
            System.out.println("文件夹不存在或无法访问");
            return Result.error("200","文件夹不存在或无法访问");

        }

        // 构造返回数据
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("filePath", ip + ":" + nginx_port + newZipFilePath);
        resultMap.put("newVersion", NewDocumentVersion);

        return Result.success(resultMap);


    }
    @PostMapping("/getDocumentList")
    public Result<?> getDocumentList(@RequestParam("document") String documentStr) throws IOException {
        System.out.println(documentStr);
        String unescape = HtmlUtil.unescape(documentStr);//还原成json字符串
        Document document = JSONUtil.toBean(unescape, Document.class);//Json转对象
        System.out.println(document);

        //声明返回变量
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> fileList = new ArrayList<>();
        String zipFilePath = "";
        // 获取 drawingEntity 中的字段值
        Integer id = document.getId();
        String itemNo = document.getItemNo();
        String materialNo = document.getMaterialNo();
        Integer documentType = document.getDocumentType();
        String fileNameWithoutExtension = document.getDocumentName();
        String fileVersion = document.getDocumentVersion();
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
        // 首先找到待更改的文件 拼接出目标文件夹的路径
        //String folderPath = "/files/";
        String folderPath = file_location;
        //判断一下料号是否为空 如果为空 则表示为备产产品 如果不为空则表示标准生产订单产品
        if (StringUtils.isNotBlank(materialNo)) {
            //判断一下有没有项目号  有项目号创建项目号路径
            folderPath += itemNo + "/" + materialNo + "/" +  documentTypeName;
        } else {
            folderPath += itemNo + "/"  + documentTypeName;
        }

        // 获取文件夹下所有文件
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        // 遍历文件数组，判断是否存在匹配的文件名（不带后缀）
        if (files != null) {
            for (File onefile : files) {
                String fileName = onefile.getName();
                int dotIndex = fileName.lastIndexOf('.');
                String nameWithoutExtension = (dotIndex > 0 && dotIndex < fileName.length() - 1)
                        ? fileName.substring(0, dotIndex)
                        : fileName;

                if (nameWithoutExtension.equals(fileNameWithoutExtension + fileVersion)) {
                    // 匹配到相同文件名（不带后缀）的文件
                    System.out.println("文件存在");
                    //获取文件的后缀
                    String extension = getFileExtension(onefile.getName());
                    //如果文件的后缀不为空 即获取成功
                    if (extension != ""){
                        // 判断文件类型是否为压缩文件
                        if (extension.equals("rar") || extension.equals("zip")) {
                            if (extension.equals("rar")){
                                //  如果是压缩文件 处理压缩文件的逻辑
                                // 首先核对压缩包中是否含有和上传的文件同名的文件
                                zipFilePath = folderPath + "/" + fileName;
                                fileList = getRarFileList(zipFilePath);
                            } else if ( extension.equals("zip")) {
                                //  如果是压缩文件 处理压缩文件的逻辑
                                // 首先核对压缩包中是否含有和上传的文件同名的文件
                                zipFilePath = folderPath + "/" + fileName;
                                fileList = getZipFileList(zipFilePath);
                            }



                            System.out.println("文件是压缩文件");
                        } else {
                            //如果不是压缩文件
                            System.out.println("文件不是压缩文件");
                            return Result.error("202","被更新的文件不是压缩文件");

                        }
                    }else {
                        //如果获取文件的后缀名失败
                        return Result.error("203","文件后缀获取失败");
                    }



                    break;
                }
            }
        } else {
            System.out.println("文件夹不存在或无法访问");
            return Result.error("200","文件夹不存在或无法访问");

        }

        // 构造返回数据
        // 构造返回数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", 1);
        responseData.put("filepath", zipFilePath);
        responseData.put("subFilePath", fileList);
        resultList.add(responseData);
        // 转化为JSON字符串
        String jsonResponse = JSONUtil.toJsonStr(resultList);
        System.out.println(jsonResponse);
        return Result.success(resultList);
    }
    @PostMapping("/deleteOneFileFromZip")
    public Result<?> deleteOneFileFromZip(@RequestParam("document") String documentStr, @RequestParam("fileListStr") String fileListStr) throws IOException {

            //声明返回变量
            String newZipFilePath = "";
            String NowDocumentVersion = "";
            String NewDocumentVersion = "";


        //0 处理传入的删除文件的名单
            List<String> filesToDeleteList = new LinkedList<>();
            // 拆分字符串并转换为整数
            String[] filesToDelete = fileListStr.split(",");
            for (String fileToDelete : filesToDelete) {
                if (!fileToDelete.isEmpty()) {
                    try {
                        filesToDeleteList.add(fileToDelete);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

        //1 先找到压缩包
            System.out.println(documentStr);
            String unescape = HtmlUtil.unescape(documentStr);//还原成json字符串
            Document document = JSONUtil.toBean(unescape, Document.class);//Json转对象
            System.out.println(document);

            //声明返回变量
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> fileList = new ArrayList<>();
            String zipFilePath = "";
            // 获取 drawingEntity 中的字段值
            Integer id = document.getId();
            String itemNo = document.getItemNo();
            String materialNo = document.getMaterialNo();
            Integer documentType = document.getDocumentType();
            String fileNameWithoutExtension = document.getDocumentName();
            String fileVersion = document.getDocumentVersion();
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
            // 首先找到待更改的文件 拼接出目标文件夹的路径
            //String folderPath = "/files/";
            String folderPath = file_location;
            //判断一下料号是否为空 如果为空 则表示为备产产品 如果不为空则表示标准生产订单产品
            if (StringUtils.isNotBlank(materialNo)) {
                //判断一下有没有项目号  有项目号创建项目号路径
                folderPath += itemNo + "/" + materialNo + "/" +  documentTypeName;
            } else {
                folderPath += itemNo + "/"  + documentTypeName;
            }

            // 获取文件夹下所有文件
            File folder = new File(folderPath);
            File[] files = folder.listFiles();
            // 遍历文件数组，判断是否存在匹配的文件名（不带后缀）
            if (files != null) {
                for (File onefile : files) {
                    String fileName = onefile.getName();
                    int dotIndex = fileName.lastIndexOf('.');
                    String nameWithoutExtension = (dotIndex > 0 && dotIndex < fileName.length() - 1)
                            ? fileName.substring(0, dotIndex)
                            : fileName;

                    if (nameWithoutExtension.equals(fileNameWithoutExtension + fileVersion)) {
                        // 匹配到相同文件名（不带后缀）的文件
                        System.out.println("文件存在");
                        //获取文件的后缀
                        String extension = getFileExtension(onefile.getName());
                        //如果文件的后缀不为空 即获取成功
                        if (extension != ""){
                            // 判断文件类型是否为压缩文件
                            if (extension.equals("rar") || extension.equals("zip")) {
                                //  如果是压缩文件 处理压缩文件的逻辑
                                //2 复制压缩包

                                //获取压缩包的路径
                                zipFilePath = folderPath + "/" + fileName;

                                // 复制压缩文件
                                String copiedZipFilePath = folderPath + "/copied_" + fileName;
                                copyFile(zipFilePath, copiedZipFilePath);

                                // 解压缩被复制的压缩文件
                                String extractedFolderPath = folderPath + "/" + fileNameWithoutExtension + "/extracted";
                                unzipFile(copiedZipFilePath, extractedFolderPath);

                                // 根据删除文件名单去解压后的文件夹中删除文件
                                deleteOneFile(filesToDeleteList,extractedFolderPath);


                                // 重新打包压缩文件 这个新的压缩文件就是替换完文件之后的压缩包 也就是新版本的文件
                                NowDocumentVersion =  documentService.getNowDocumentVersion(id);
                                NewDocumentVersion = "A" + String.format("%02d", Integer.parseInt(NowDocumentVersion.substring(1)) + 1);
                                // 获取不包含后缀的文件名
                                String baseName = fileName.substring(0, fileName.lastIndexOf("."));
                                //获取不包含版本信息的文件名
                                String desiredPart = baseName.substring(0, baseName.length() - 3);


                                //newZipFilePath = folderPath + "/" +desiredPart
                                //        + fileName.substring(fileName.lastIndexOf("."));
                                newZipFilePath = folderPath + "/" +desiredPart
                                        + ".zip";
                                zipFile(extractedFolderPath, newZipFilePath);

                                // 删除解压产生的文件夹
                                File extractedFolder = new File(folderPath + "/" + fileNameWithoutExtension);
                                deleteFolder(extractedFolder);

                                // 删除复制的压缩文件
                                File copiedZipFile = new File(copiedZipFilePath);
                                if (copiedZipFile.exists()) {
                                    copiedZipFile.delete();
                                }

                                System.out.println("处理压缩文件的逻辑完成");






                                System.out.println("文件是压缩文件");
                            } else {
                                //如果不是压缩文件
                                System.out.println("文件不是压缩文件");
                                return Result.error("202","被更新的文件不是压缩文件");

                            }
                        }else {
                            //如果获取文件的后缀名失败
                            return Result.error("203","文件后缀获取失败");
                        }



                        break;
                    }
                }
            } else {
                System.out.println("文件夹不存在或无法访问");
                return Result.error("200","文件夹不存在或无法访问");

            }






        // 构造返回数据

        // 构造返回数据
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("filePath", ip + ":" + nginx_port + newZipFilePath);
        resultMap.put("newVersion", NewDocumentVersion);

        return Result.success(resultMap);
    }

    public static boolean isFileInZip(String zipFilePath, String targetFileName) throws IOException {
        File file = new File(zipFilePath);




        if (file.getName().toLowerCase().endsWith(".zip")){
            // 处理 zip 文件的逻辑
            ZipEntry ze = null;
            Path path = Paths.get(zipFilePath);

            try (
                    InputStream inputStream = Files.newInputStream(path);
                    ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
            ){
                while ((ze = zis.getNextEntry()) != null) {
                    if (!ze.isDirectory() && targetFileName.contains(removeFileExtension(ze.getName()))) {
                        return true;
                    }
                    zis.closeEntry(); // 关闭当前entry的流
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        } else if (file.getName().toLowerCase().endsWith(".rar")) {
            try {
                IInArchive inArchive;
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

                inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
                ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

                for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                    System.out.println(" this is contains path:" + item.getPath());
                    if (!item.isFolder() && targetFileName.contains(removeFileExtension(item.getPath()))) {
                        inArchive.close();
                        randomAccessFile.close();
                        return true;
                    }
                }

                inArchive.close();
                randomAccessFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }



    // 复制压缩包文件
    private static void copyFile(String sourceFilePath, String destinationFilePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(sourceFilePath));
             OutputStream outputStream = Files.newOutputStream(Paths.get(destinationFilePath))) {
            // 清空目标文件的内容
            Files.write(Paths.get(destinationFilePath), new byte[0]);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            // 处理异常
        }
    }
    //从指定文件夹中删除
    private static void deleteOneFile( List<String> filesToDeleteList, String extractedFolderPath) throws IOException {
        for (String fileToDelete : filesToDeleteList) {
            //extractedFolderPath是指定文件夹的路径，fileToDelete是要删除的文件名
            File extractedFile = new File(extractedFolderPath + "/" + fileToDelete);
            if (extractedFile.exists()) {
                if (extractedFile.delete()) {
                    System.out.println("Deleted file: " + fileToDelete);
                } else {
                    System.out.println("Failed to delete file: " + fileToDelete);
                }
            } else {
                System.out.println("File not found: " + fileToDelete);
            }
        }
    }
    // 解压缩文件
    private static void unzipFile(String zipFilePath, String destinationFolderPath) throws IOException {
        File file = new File(zipFilePath);
        String zipfileName = file.getName();
        // 将文件名转换为小写形式 先使用后缀名进行判断
        String lowerCaseFileName = zipfileName.toLowerCase();
        if (lowerCaseFileName.endsWith(".zip")) {
            String type ;
            try {

                FileInputStream inputStream1 = new FileInputStream(file);
                type = FileTypeUtil.getType(inputStream1);
                inputStream1.close();

                //根据首部字节判断文件类型
                if ("zip".contains(type)){
                    //文件类型判断成功

                    // 处理 zip 文件的逻辑
                    ZipEntry entry = null;
                    Path path = Paths.get(zipFilePath);


                    try (
                            InputStream inputStream = Files.newInputStream(path);
                            ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
                    ){
                        File destinationFolder = new File(destinationFolderPath);
                        if (!destinationFolder.exists()) {
                            destinationFolder.mkdirs();
                        }

                        byte[] buffer = new byte[1024];
                        while ((entry = zis.getNextEntry()) != null) {
                            if (!entry.isDirectory()) {
                                String entryName = entry.getName();
                                File entryFile = new File(destinationFolderPath + "/" + entryName);
                                File entryParent = entryFile.getParentFile();
                                if (entryParent != null && !entryParent.exists()) {
                                    entryParent.mkdirs();
                                }

                                try (OutputStream outputStream = new FileOutputStream(entryFile)) {
                                    int length;
                                    while ((length = zis.read(buffer)) > 0) {
                                        outputStream.write(buffer, 0, length);
                                    }
                                }
                            }
                            zis.closeEntry();
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        else if (lowerCaseFileName.endsWith(".rar")) {
            // 处理 rar 文件的逻辑
            String type;

            FileInputStream inputStream1 = new FileInputStream(file);
            type = FileTypeUtil.getType(inputStream1);
            inputStream1.close();
            //根据首部字节判断文件类型
            if ("rar".contains(type)) {
                // 处理 rar 文件的逻辑
                File destinationFolder = new File(destinationFolderPath);
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs();
                    System.out.println("destinationFolder" + destinationFolder.getPath());
                }
                RandomAccessFile randomAccessFile = null;
                IInArchive archive = null;

                try {
                    randomAccessFile = new RandomAccessFile(file, "r");
                    archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

                    int[] in = new int[archive.getNumberOfItems()];
                    for (int i = 0; i < in.length; i++) {
                        in[i] = i;
                    }
                    archive.extract(in, false, new ExtractCallback(archive,destinationFolder.getPath()));
                }catch (FileNotFoundException | SevenZipException e) {
                    e.printStackTrace();
                } finally {
                    if (archive != null) {
                        try {
                            archive.close();
                        } catch (SevenZipException e) {
                            System.err.println("Error closing archive: " + e);
                        }
                    }
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e) {
                            System.err.println("Error closing file: " + e);
                        }
                    }
                }
            }
        }




    }
    // 压缩文件
    private static void zipFile(String folderPath, String zipFilePath) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            addFilesToZip(folderPath, "", zipOutputStream);
        }
    }
    // 替换文件
    //folderPath 源文件解压之后的文件夹路径
    //targetFileName 传入的文件的文件名
    //replacementFilePath 传入的文件的文件路径
    private static void replaceFile(String folderPath, String targetFileName, String replacementFilePath) throws IOException {
        File folder = new File(folderPath);
        //源文件解压之后文件夹中的文件列表
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileNameWithoutExtension = removeFileExtension(file.getName());
                if (targetFileName.contains(fileNameWithoutExtension)) {
                    copyFile(replacementFilePath,file.getAbsolutePath());
                    break;
                }
            }
        }
    }

    private static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
    // 递归添加文件到压缩包
    private static void addFilesToZip(String folderPath, String parentPath, ZipOutputStream zipOutputStream) throws IOException {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            byte[] buffer = new byte[1024];
            for (File file : files) {
                if (file.isDirectory()) {
                    String subFolderPath = parentPath + file.getName() + "/";
                    addFilesToZip(file.getAbsolutePath(), subFolderPath, zipOutputStream);
                } else {
                    String entryName = parentPath + file.getName();
                    ZipEntry entry = new ZipEntry(entryName);
                    zipOutputStream.putNextEntry(entry);

                    try (InputStream inputStream = new FileInputStream(file)) {
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, length);
                        }
                    }

                    zipOutputStream.closeEntry();
                }
            }
        }
    }

    // 删除文件夹的递归方法
    private static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }

    // 获取文件的扩展名
    private static List<Map<String, Object>> getZipFileList(String zipFilePath) throws IOException {

        List<Map<String, Object>> fileList = new ArrayList<>();



        // 处理 zip 文件的逻辑
        ZipEntry ze = null;
        Path path = Paths.get(zipFilePath);

        try (
                InputStream inputStream = Files.newInputStream(path);
                ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("gbk"));
        ){
            int fileId = 1;
            while((ze=zis.getNextEntry())!=null){
                String fileName = ze.getName();
                Map<String, Object> fileData = new HashMap<>();
                List<Map<String, Object>> resultList = new ArrayList<>();
                fileData.put("id", fileId);
                fileData.put("filepath", fileName);
                fileData.put("subFilePath",resultList);
                fileList.add(fileData);
                zis.closeEntry();
                fileId++;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }
    private static List<Map<String, Object>> getRarFileList(String zipFilePath) {
        File file = new File(zipFilePath);
        List<Map<String, Object>> fileList = new ArrayList<>();
        try {
            IInArchive inArchive;
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

            inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
            int fileId = 1;
            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                System.out.println(" this is contains path:" + item.getPath());
                String fileName = item.getPath();
                Map<String, Object> fileData = new HashMap<>();
                List<Map<String, Object>> resultList = new ArrayList<>();
                fileData.put("id", fileId);
                fileData.put("filepath", fileName);
                fileData.put("subFilePath",resultList);
                fileList.add(fileData);
                fileId++;
            }

            inArchive.close();
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileList;
    }




    // 获取文件的扩展名
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/updateDocumentFilesM")
    public Result<?> updateDocumentFilesM(@RequestParam("file") MultipartFile file,
                                          @RequestParam("document") String documentStr) throws IOException{

        String unescape = HtmlUtil.unescape(documentStr);//还原成json字符串
        Document document = JSONUtil.toBean(unescape, Document.class);//Json转对象
        System.out.println(document);

        // 获取 document 中的字段值

        String itemNo = document.getItemNo();
        String materialNo = document.getMaterialNo();


        List<String> updateFilesName =  new ArrayList<>();

        //下面要对压缩包的类型进行判断
        //首先获取文件的原始名称
        // 获取文件名
        String OriginalfileName = file.getOriginalFilename();

        // 将文件名转换为小写形式 先使用后缀名进行判断
        String lowerCaseFileName = OriginalfileName.toLowerCase();

        if (lowerCaseFileName.endsWith(".zip")) {

            String type ;
            try {

                // 打开输入流获取文件类型
                try (InputStream inputStream = file.getInputStream()) {
                    type = FileTypeUtil.getType(inputStream);
                }

                //根据首部字节判断文件类型
                if ("zip".contains(type)){
                    //文件类型判断成功

                    // 处理 zip 文件的逻辑
                    ZipEntry ze = null;
                    try (
                            InputStream inputStream1 = file.getInputStream();
                            ZipInputStream zis = new ZipInputStream(inputStream1, Charset.forName("gbk"));
                    ){

                        while((ze=zis.getNextEntry())!=null) {
                            //获取压缩包中文件的名称 这个名称中包含这文件类型和文件序号的信息
                            String fileName = ze.getName();
                            String fileExt = "";
                            if(fileName.endsWith("/")) {
                                //此处判断如果压缩包一级目录中有文件夹 则直接报错 zip文件不符合格式 这个文件夹也不处理
                                continue;
                            }else{
                                //获取文件名的后缀
                                int dotPos = fileName.lastIndexOf(".");
                                if (dotPos >= 0) {
                                    fileExt = fileName.substring(dotPos);
                                }


                                // 判断文件名中是否包含字符串，如果包含，根据映射关系设置 drawingType 的值
                                Integer documentType = null;
                                String documentTypeName = null;
                                if (fileName.contains("材料清单") || fileName.contains("清单")) {
                                    documentType = 1;
                                } else if (fileName.contains("装配工艺图") || fileName.contains("工艺")) {
                                    documentType = 2;
                                } else if (fileName.contains("电气接线图") || fileName.contains("图纸")) {
                                    documentType = 3;
                                } else if (fileName.contains("变更通知单") || fileName.contains("变更单")) {
                                    documentType = 4;
                                } else if (fileName.contains("技术交底单") || fileName.contains("交底单")) {
                                    documentType = 5;
                                } else {
                                    System.out.println("文件名不符合要求");
                                    // 文件名不符合要求，直接跳过
                                    continue;
                                }

                                // 根据类型值获取对应的汉字
                                Map<Integer, String> documentTypeMap = new HashMap<>();
                                documentTypeMap.put(1, "BOM");
                                documentTypeMap.put(2, "APD");
                                documentTypeMap.put(3, "EWD");
                                documentTypeMap.put(4, "CN");
                                documentTypeMap.put(5, "TDF");
                                if (documentTypeMap.containsKey(documentType)) {
                                    documentTypeName = documentTypeMap.get(documentType);
                                }else {
                                    continue;
                                }



                                Integer sequenceNo = null;
                                // 使用正则表达式匹配文件名中的数字
                                Pattern pattern = Pattern.compile("_(\\d+)\\.");
                                Matcher matcher = pattern.matcher(fileName);
                                if (matcher.find()) {
                                    // 匹配成功，获取到数字，并根据数字设置 drawingType 和 sequenceNo 的值
                                    sequenceNo = Integer.parseInt(matcher.group(1));
                                } else {
                                    System.out.println("序号不对");
                                    // 文件名不符合要求，直接跳过
                                    continue;
                                }


                                //拼接文件夹路径
                                //String folderPath = "/files/";
                                String folderPath = nginx_location;
                                String filePath = "";
                                //判断一下料号是否为空 如果为空 则表示为备产产品 如果不为空则表示标准生产订单产品
                                if (StringUtils.isNotBlank(materialNo)) {
                                    //判断一下有没有项目号  有项目号创建项目号路径
                                    folderPath +=   itemNo + "/" + materialNo + "/" +  documentTypeName;
                                    File fileDirectory = new File(folderPath);  //这里需要先创建对应的文件夹
                                    if(!fileDirectory.exists()) {
                                        fileDirectory.mkdirs();
                                    }
                                    filePath = folderPath + "/"
                                            + itemNo + "_" + materialNo + "_" + documentTypeName + "_" + sequenceNo
                                            + fileExt ;
                                } else {
                                    folderPath += itemNo + "/"  + documentTypeName;
                                    File fileDirectory = new File(folderPath);  //这里需要先创建对应的文件夹
                                    if(!fileDirectory.exists()) {
                                        fileDirectory.mkdirs();
                                    }
                                    filePath = folderPath + "/"
                                            + itemNo + "_" + documentTypeName + "_" + sequenceNo
                                            + fileExt ;
                                }
                                File realFile = new File(filePath);
                                //如果文件不是文件夹 而是文件
                                if(!realFile.exists()) {
                                    // Create File
                                    boolean fileCreated = realFile.createNewFile();
                                    if (!fileCreated) {
                                        throw new IOException("Unable to create file at specified path. It already exists");
                                    }


                                }
                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(realFile));
                                byte[] b = new byte[10240];
                                int len;
                                while((len = zis.read(b))>0) {
                                    bos.write(b, 0, len);
                                }
                                bos.close();

                                //这里在对filePath拼接之前需要先处理一下
                                filePath = file_location + filePath.replaceFirst("^" + nginx_location, "");
                                //每成功上传一次要添加两个元素 一个是文件的路径 一个是文件的原始文件名
                                updateFilesName.add(ip  + ":" + nginx_port +filePath);
                                updateFilesName.add(fileName);

                                System.out.println("filePath:"+filePath);
                            }
                            zis.closeEntry();
                        }
                        zis.close();
                        return Result.success("0","全部上传",updateFilesName);
                    }catch (IOException e){
                        e.printStackTrace();
                        return Result.error("201","压缩包解析失败");

                    }

                }else {
                    return Result.error("201","压缩包解析失败");
                }
            } catch (Exception e) {
                return Result.error("201","压缩包解析失败");
            }




        } else if (lowerCaseFileName.endsWith(".rar")) {
            // 处理 rar 文件的逻辑
            String type;
            InputStream inputStream = file.getInputStream();
            type = FileTypeUtil.getType(inputStream);
            inputStream.close();
            //根据首部字节判断文件类型
            if ("rar".contains(type)) {
                // 处理 rar 文件的逻辑


                //存放rar文件的临时文件夹
                String tempDirZip = System.getProperty("java.io.tmpdir");
                System.out.println("tempDirZip" + tempDirZip);
                String prefixZip = tempDirZip + File.separator + IdUtil.fastSimpleUUID();
                System.out.println("prefixZip" + prefixZip);
                File fileDirectoryConvertZip = new File(prefixZip);

                if (!fileDirectoryConvertZip.exists()) {
                    fileDirectoryConvertZip.mkdirs();
                    System.out.println("fileDirectoryConvertZip" + fileDirectoryConvertZip.getPath());
                }

                //用来存放解压之后的文件的路径
                String tempDir = System.getProperty("java.io.tmpdir");
                System.out.println("tempDir"   + tempDir);
                String prefix = tempDir + File.separator+ IdUtil.fastSimpleUUID();
                System.out.println("prefix" + prefix);
                File fileDirectoryConvert = new File(prefix);

                if (!fileDirectoryConvert.exists()) {
                    fileDirectoryConvert.mkdirs();
                    System.out.println("fileDirectoryConvert" + fileDirectoryConvert.getPath());
                }

                File convertedFile = new File(prefixZip + File.separator + file.getOriginalFilename());
                System.out.println("convertedFile" + convertedFile.getPath());

                InputStream inputStream1 = file.getInputStream();

                FileUtils.copyInputStreamToFile(inputStream1, convertedFile);

                inputStream1.close();

                //IInArchive archive;
                //RandomAccessFile randomAccessFile;
                //// 第一个参数是需要解压的压缩包路径，第二个参数参考JdkAPI文档的RandomAccessFile
                ////r代表以只读的方式打开文本，也就意味着不能用write来操作文件
                //randomAccessFile = new RandomAccessFile(convertedFile, "r");
                //archive = SevenZip.openInArchive(null, // null - autodetect
                //        new RandomAccessFileInStream(randomAccessFile));
                //int[] in = new int[archive.getNumberOfItems()];
                //for (int i = 0; i < in.length; i++) {
                //    in[i] = i;
                //}
                //System.out.println("in[int]" + Arrays.toString(in));
                //archive.extract(in, false, new ExtractCallback(archive,fileDirectoryConvert.getPath()));
                //archive.close();
                //randomAccessFile.close();

                RandomAccessFile randomAccessFile = null;
                IInArchive archive = null;
                // 第一个参数是需要解压的压缩包路径，第二个参数参考JdkAPI文档的RandomAccessFile
                //r代表以只读的方式打开文本，也就意味着不能用write来操作文件
                //randomAccessFile = new RandomAccessFile(convertedFile, "r");
                //archive = SevenZip.openInArchive(null, // null - autodetect
                //        new RandomAccessFileInStream(randomAccessFile));
                //int[] in = new int[archive.getNumberOfItems()];
                //for (int i = 0; i < in.length; i++) {
                //    in[i] = i;
                //}
                //System.out.println("in[int]" + Arrays.toString(in));
                //archive.extract(in, false, new ExtractCallback(archive,fileDirectoryConvert.getPath()));
                try {
                    randomAccessFile = new RandomAccessFile(convertedFile, "r");
                    archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

                    int[] in = new int[archive.getNumberOfItems()];
                    for (int i = 0; i < in.length; i++) {
                        in[i] = i;
                    }
                    archive.extract(in, false, new ExtractCallback(archive,fileDirectoryConvert.getPath()));
                }catch (FileNotFoundException | SevenZipException e) {
                    e.printStackTrace();
                } finally {
                    if (archive != null) {
                        try {
                            archive.close();
                        } catch (SevenZipException e) {
                            System.err.println("Error closing archive: " + e);
                        }
                    }
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e) {
                            System.err.println("Error closing file: " + e);
                        }
                    }
                }



                convertedFile.delete(); // 删除原始压缩包文件
                fileDirectoryConvertZip.delete();

                // 获取解压后的文件名
                File[] files = fileDirectoryConvert.listFiles();
                String fileExt = "";
                System.out.println("fileExt" + fileExt);
                if (files != null) {
                    System.out.println("压缩包不为空");
                    for (File extractedFile : files) {
                        System.out.println("开始遍历");
                        if (extractedFile.isDirectory()) {
                            System.out.println("内含文件夹？？");
                            continue;
                        } else {
                            System.out.println("extractedFile" + extractedFile.getPath());

                            String fileName = extractedFile.getName();
                            System.out.println("fileName" + fileName);
                            // 在这里处理每个文件的文件名，可以打印、保存到数据库等
                            System.out.println("Extracted file name: " + fileName);

                            //获取文件名的后缀
                            int dotPos = fileName.lastIndexOf(".");
                            if (dotPos >= 0) {
                                fileExt = fileName.substring(dotPos);
                            }


                            // 判断文件名中是否包含字符串，如果包含，根据映射关系设置 drawingType 的值
                            Integer documentType = null;
                            String documentTypeName = null;
                            if (fileName.contains("材料清单") || fileName.contains("清单")) {
                                documentType = 1;
                            } else if (fileName.contains("装配工艺图") || fileName.contains("工艺")) {
                                documentType = 2;
                            } else if (fileName.contains("电气接线图") || fileName.contains("图纸")) {
                                documentType = 3;
                            } else if (fileName.contains("变更通知单") || fileName.contains("变更单")) {
                                documentType = 4;
                            } else if (fileName.contains("技术交底单") || fileName.contains("交底单")) {
                                documentType = 5;
                            } else {
                                System.out.println("文件名不符合要求");
                                // 文件名不符合要求，直接跳过
                                continue;
                            }

                            // 根据类型值获取对应的汉字
                            Map<Integer, String> documentTypeMap = new HashMap<>();
                            documentTypeMap.put(1, "BOM");
                            documentTypeMap.put(2, "APD");
                            documentTypeMap.put(3, "EWD");
                            documentTypeMap.put(4, "CN");
                            documentTypeMap.put(5, "TDF");
                            if (documentTypeMap.containsKey(documentType)) {
                                documentTypeName = documentTypeMap.get(documentType);
                            } else {
                                continue;
                            }


                            Integer sequenceNo = null;
                            // 使用正则表达式匹配文件名中的数字
                            Pattern pattern = Pattern.compile("_(\\d+)\\.");
                            Matcher matcher = pattern.matcher(fileName);
                            if (matcher.find()) {
                                // 匹配成功，获取到数字，并根据数字设置 drawingType 和 sequenceNo 的值
                                sequenceNo = Integer.parseInt(matcher.group(1));
                            } else {
                                System.out.println("序号不对");
                                // 文件名不符合要求，直接跳过
                                continue;
                            }


                            //拼接文件夹路径
                            //String folderPath = "/files/";
                            String folderPath = nginx_location;
                            String filePath = "";
                            //判断一下料号是否为空 如果为空 则表示为备产产品 如果不为空则表示标准生产订单产品
                            if (StringUtils.isNotBlank(materialNo)) {
                                //判断一下有没有项目号  有项目号创建项目号路径
                                folderPath += itemNo + "/" + materialNo + "/" + documentTypeName;
                                File fileDirectory = new File(folderPath);  //这里需要先创建对应的文件夹
                                if (!fileDirectory.exists()) {
                                    fileDirectory.mkdirs();
                                }
                                filePath = folderPath + "/"
                                        + itemNo + "_" + materialNo + "_" + documentTypeName + "_" + sequenceNo
                                        + fileExt;
                            } else {
                                folderPath += itemNo + "/" + documentTypeName;
                                File fileDirectory = new File(folderPath);  //这里需要先创建对应的文件夹
                                if (!fileDirectory.exists()) {
                                    fileDirectory.mkdirs();
                                }
                                filePath = folderPath + "/"
                                        + itemNo + "_" + documentTypeName + "_" + sequenceNo
                                        + fileExt;
                            }

                            File realFile = new File(filePath);// 复制文件内容到目标文件
                            FileUtils.copyFile(extractedFile, realFile);

                            //这里在对filePath拼接之前需要先处理一下
                            filePath = file_location + filePath.replaceFirst("^" + nginx_location, "");
                            //每成功上传一次要添加两个元素 一个是文件的路径 一个是文件的原始文件名
                            updateFilesName.add(ip  + ":" + nginx_port +filePath);
                            updateFilesName.add(fileName);
                            System.out.println("filePath:"+filePath);




                        }
                    }
                    //返回之前先把临时文件夹中文件和这临时文件夹都删除
                    fileDirectoryConvert.delete();
                    return Result.success("0","全部上传",updateFilesName);
                }
                else {
                    System.out.println("压缩包中无文件");
                }
            }
        }else {
            // 处理其他类型的文件的逻辑
            return Result.error("201","非ZIP和RAR格式");
        }
        return Result.error("201","压缩包解析失败");
    }
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
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
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
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
    }



    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
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
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
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

    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
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
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delDocumentFiles")
    public Result<?> delDocumentFiles(@RequestBody List<String> filePaths){

        boolean flag = true; // 标记是否全部删除成功
        System.out.println(filePaths.size());
        for (String filePath : filePaths) {
            File fileToDelete = new File(nginx_location
                    + filePath.substring(filePath.indexOf(file_location) + file_location.length()-1));
            boolean delete = fileToDelete.delete();
            if (delete) {
                System.out.println("文件删除成功");
            } else {
                System.out.println("文件删除失败");
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
