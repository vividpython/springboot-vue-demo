package com.example.demo.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
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
    public Result<?> uploadDrawingFiles(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        //定义文件的唯一标识
        String flag = IdUtil.fastSimpleUUID();
        //FileUtil.writeBytes(file.getBytes(), new File(ip + "/" ,  flag + "_" + originalFilename));
        //String rootFilePath = ip + "/" + flag + "_" + originalFilename;
        ////if ("localhost".equals(ip)) {
        ////    rootFilePath = "C:\\files\\" + flag + "_" + originalFilename;
        ////} else {
        ////    rootFilePath = "\\\\" + ip + "\\C$\\files\\" + flag + "_" + originalFilename;
        ////}
        String rootFilePath ="/files/" + flag + "_" + originalFilename;
        FileUtil.writeBytes(file.getBytes(), rootFilePath);
        return Result.success(ip + "/"  + flag + "_" + originalFilename );
    }
    @PostMapping("/delDrawingFile")
    public Result<?> delDrawingFile(@RequestBody String delDrawingPath){
        String filePath = "C:/files/" + delDrawingPath.replace("{\"delDrawingPath\":\"" + ip + "/", "").replace("\"}","");
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
