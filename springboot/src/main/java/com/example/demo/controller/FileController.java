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
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


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


    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
            folderPath += itemNo  + documentTypeName;
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
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
                type = FileTypeUtil.getType(file.getInputStream());
                //根据首部字节判断文件类型
                if ("zip".contains(type)){
                    //文件类型判断成功

                    // 处理 zip 文件的逻辑
                    ZipEntry ze = null;
                    try (
                            ZipInputStream zis = new ZipInputStream(file.getInputStream(), Charset.forName("gbk"));
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
                                updateFilesName.add(ip  + ":" + nginx_port +filePath);
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
            type = FileTypeUtil.getType(file.getInputStream());
            //根据首部字节判断文件类型
            if ("rar".contains(type)) {
                // 处理 rar 文件的逻辑


                //存放rar文件的临时文件夹
                String tempDirZip = System.getProperty("java.io.tmpdir");
                String prefixZip = tempDirZip + File.separator + IdUtil.fastSimpleUUID();
                File fileDirectoryConvertZip = new File(prefixZip);

                if (!fileDirectoryConvertZip.exists()) {
                    fileDirectoryConvertZip.mkdirs();
                }

                //用来存放解压之后的文件的路径
                String tempDir = System.getProperty("java.io.tmpdir");
                String prefix = tempDir + File.separator + IdUtil.fastSimpleUUID();
                File fileDirectoryConvert = new File(prefix);

                if (!fileDirectoryConvert.exists()) {
                    fileDirectoryConvert.mkdirs();
                }

                File convertedFile = new File(prefixZip + File.separator + file.getOriginalFilename());
                FileUtils.copyInputStreamToFile(file.getInputStream(), convertedFile);
                IInArchive archive;
                RandomAccessFile randomAccessFile;
                // 第一个参数是需要解压的压缩包路径，第二个参数参考JdkAPI文档的RandomAccessFile
                //r代表以只读的方式打开文本，也就意味着不能用write来操作文件
                randomAccessFile = new RandomAccessFile(convertedFile, "r");
                archive = SevenZip.openInArchive(null, // null - autodetect
                        new RandomAccessFileInStream(randomAccessFile));
                int[] in = new int[archive.getNumberOfItems()];
                for (int i = 0; i < in.length; i++) {
                    in[i] = i;
                }
                archive.extract(in, false, new ExtractCallback(archive,fileDirectoryConvert.getPath()));
                archive.close();
                randomAccessFile.close();

                convertedFile.delete(); // 删除原始压缩包文件
                fileDirectoryConvertZip.delete();

                // 获取解压后的文件名
                File[] files = fileDirectoryConvert.listFiles();
                String fileExt = "";
                if (files != null) {
                    for (File extractedFile : files) {

                        if (extractedFile.isDirectory()) {
                            continue;
                        } else {

                            String fileName = extractedFile.getName();
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
                            updateFilesName.add(ip  + ":" + nginx_port +filePath);
                            System.out.println("filePath:"+filePath);




                        }
                    }
                    //返回之前先把临时文件夹中文件和这临时文件夹都删除
                    fileDirectoryConvert.delete();
                    return Result.success("0","全部上传",updateFilesName);
                }
            }
        }else {
            // 处理其他类型的文件的逻辑
            return Result.error("201","压缩包解析失败");
        }
        return Result.error("201","压缩包解析失败");
    }
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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



    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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

    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
    @RequiresRoles(logical = Logical.OR, value = {"admin", "designer"})
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
