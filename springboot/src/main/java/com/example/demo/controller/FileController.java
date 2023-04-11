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
import com.example.demo.entity.Drawing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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


    @Value("${file.ip}")
    private  String ip ;

    @PostMapping("/uploadUserIcons")
    public Result<?> uploadUserIcons(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        //定义文件的唯一标识
        String flag = IdUtil.fastSimpleUUID();
        //String rootFilePath = System.getProperty("user.dir") + "/springboot/src/main/resources/userIcons/" + flag + "_" + originalFilename;
        String rootFilePath ="/files/" + flag + "_" + originalFilename;
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
        String rootFilePath ="/files/" + flag + "_" + originalFilename;
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

    @PostMapping("/uploadDrawingFiles")
    public Result<?> uploadDrawingFiles(@RequestParam("file") MultipartFile file,@RequestParam("drawing") String drawingStr) throws IOException {
        String originalFilename = file.getOriginalFilename();
        System.out.println(drawingStr);
        String unescape = HtmlUtil.unescape(drawingStr);//还原成json字符串
        Drawing drawing = JSONUtil.toBean(unescape, Drawing.class);//Json转对象
        System.out.println(drawing);

        // 获取当前时间的年月
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String month = sdf.format(new Date());

        // 获取 drawingEntity 中的字段值
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
        String folderPath = "/files/" + month;

        if (StringUtils.isNotBlank(itemNo)) {
            //判断一下有没有项目号  有项目号创建项目号路径
            folderPath += "/" + itemNo + "/" + productNo + "/" + drawingTypeName;
        } else {
            folderPath += "/" + productNo + "/" + drawingTypeName;
        }
        // 保存文件
        String filePath = folderPath + "/" +originalFilename;
        FileUtil.writeBytes(file.getBytes(), filePath);
        return Result.success(ip + "/" + folderPath + "/"  + originalFilename );
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

//    @PostMapping("/uploadDrawingFiles")
//    public Result<?> uploadDrawingFiles(MultipartFile file, DrawingEntity drawingEntity) throws IOException {
//        String originalFilename = file.getOriginalFilename();
//// 获取当前月份
//        Calendar calendar = Calendar.getInstance();
//        int month = calendar.get(Calendar.MONTH) + 1;
//// 根据月份创建对应的文件夹
//        String monthFolder = "/files/" + month + "/";
//        File monthFolderFile = new File(monthFolder);
//        if (!monthFolderFile.exists()) {
//            monthFolderFile.mkdirs();
//        }
//// 获取项目号、产品料号、图纸类型等信息
//        String projectCode = drawingEntity.getProjectCode();
//        String productCode = drawingEntity.getProductCode();
//        int drawingTypeValue = drawingEntity.getDrawingType();
//        String drawingTypeName = "";
//// 根据类型值获取对应的汉字
//        Map<Integer, String> drawingTypeMap = new HashMap<>();
//        drawingTypeMap.put(1, "材料清单");
//        drawingTypeMap.put(2, "定屏图纸");
//        drawingTypeMap.put(3, "配线图");
//        drawingTypeMap.put(4, "技术交底单");
//        if (drawingTypeMap.containsKey(drawingTypeValue)) {
//            drawingTypeName = drawingTypeMap.get(drawingTypeValue);
//        }
//// 根据信息创建对应的文件夹
//        String projectFolder = monthFolder + projectCode + "/";
//        String productFolder = projectFolder + productCode + "/";
//        String typeFolder = productFolder + drawingTypeName + "/";
//        File projectFolderFile = new File(projectFolder);
//        File productFolderFile = new File(productFolder);
//        File typeFolderFile = new File(typeFolder);
//        if (!projectFolderFile.exists()) {
//            projectFolderFile.mkdirs();
//        }
//        if (!productFolderFile.exists()) {
//            productFolderFile.mkdirs();
//        }
//        if (!typeFolderFile.exists()) {
//            typeFolderFile.mkdirs();
//        }
//// 保存文件
//        String filePath = typeFolder + IdUtil.fastSimpleUUID() + "_" + originalFilename;
//        FileUtil.writeBytes(file.getBytes(), filePath);
//        return Result.success(ip + filePath);
//    }

    @PostMapping("/delDrawingFile")
    public Result<?> delDrawingFile(@RequestBody String delDrawingPath){

        String filePath ="C:/files/" + delDrawingPath.replace("{\"delDrawingPath\":\"","").replace("\"}","");
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
