package com.upc.modular.auth.controller;

import com.upc.common.responseparam.R;
import com.upc.common.utils.FileManageUtil;
import com.upc.modular.auth.controller.param.UploadBase64Param;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.upc.common.utils.FileManageUtil.uploadFile;

/**
 * @Author: xth
 * @Date: 2025/7/8 9:48
 */
@Slf4j
@RestController
@RequestMapping("/common")
@Api(tags = "通用方法")
public class CommonController {

    @Value("${files.path}")
    private String basePath;

    @Value("${files.apkpath:/opt/apkfile}")
    private String apkFilePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        Path folderPath = Paths.get(basePath, FileManageUtil.yyyyMMddStr());
        String fileName = FileManageUtil.createFileName(file);
        String filePath = uploadFile(file, folderPath, fileName);

        return R.ok(filePath);
    }

    /**
     * 上传base64格式图片转换成png文件
     *
     * @param base64Data
     * @return
     */
    @ApiOperation("上传base64格式图片转换成png文件")
    @PostMapping("/uploadBase64")
    public R<String> uploadBase64(@RequestBody UploadBase64Param base64Data) {
        Path folderPath = Paths.get(basePath, FileManageUtil.yyyyMMddStr());
        String pngFileName = UUID.randomUUID() + ".png";
        String data = base64Data.getBase64Data();
        return R.ok(FileManageUtil.saveBase64Image(data, folderPath, pngFileName));
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response
     */
    @ApiOperation("文件下载")
    @GetMapping("/download")
    public void download(@RequestParam String name, HttpServletResponse response) {
        try {
            //response字符流-IO流下载到页面
            FileInputStream fileInputStream = new FileInputStream(name);
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation("上传APK文件")
    @PostMapping("/uploadApk")
    public R<String> uploadApk(MultipartFile file) {
        Path folderPath = Paths.get(apkFilePath);
        String fileName = file.getOriginalFilename();
        try {
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            file.transferTo(new File(folderPath.toFile(), fileName));
            return R.ok(Paths.get(apkFilePath, fileName).toString());
        } catch (IOException e) {
            log.error("上传APK文件失败", e);
            return R.fail("上传APK文件失败");
        }
    }

    @ApiOperation("下载APK文件")
    @GetMapping("/downloadApk")
    public void downloadApk(@RequestParam String name, HttpServletResponse response) {
        Path filePath = Paths.get(apkFilePath, name);
        try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
             ServletOutputStream outputStream = response.getOutputStream()) {

            response.setContentType("application/vnd.android.package-archive");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");

            int len;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.flush();
        } catch (Exception e) {
            log.error("下载APK文件失败", e);
        }
    }

    @ApiOperation("根据文件扩展名搜索上传目录中的文件")
    @GetMapping("/files")
    public R<Map<String, Object>> searchUploadFiles(
            @RequestParam("extension") String extension,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        
        // 参数校验
        if (extension == null || extension.trim().isEmpty()) {
            return R.fail("参数 extension 不能为空");
        }
        
        // 安全检查，防止目录遍历攻击
        if (extension.contains("..") || extension.contains("/") || extension.contains("\\")) {
            return R.fail("无效的文件扩展名");
        }
        
        // 限制参数校验
        if (limit <= 0) {
            limit = 10; // 默认值
        }
        
        // 搜索文件
        //String uploadDirPath = "/opt/textbook-app/upload";
        //String uploadDirPath = "C:\\Users\\25313\\Desktop\\PostGraduate";
        String uploadDirPath = "/home/u/cjm/";
        List<File> files = searchFilesInDirectory(new File(uploadDirPath), extension);
        
        // 限制结果数量
        List<File> limitedFiles = files.stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("total", files.size());
        
        List<Map<String, String>> fileList = limitedFiles.stream().map(file -> {
            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("fileName", file.getName());
            // 构建可访问的URL
            String relativePath = file.getAbsolutePath().substring(uploadDirPath.length());
            // 原始代码 (生产环境使用):
            // fileInfo.put("url", "https://172.20.128.91" + relativePath.replace("\\", "/"));
            // 本地测试代码 (开发环境使用):
            fileInfo.put("url", "http://180.201.148.80/home/u/cjm/" + relativePath.replace("\\", "/"));
            return fileInfo;
        }).collect(Collectors.toList());
        
        result.put("files", fileList);
        
        return R.ok(result);
    }

    /**
     * 递归搜索目录及其子目录中具有指定扩展名的文件
     * 
     * @param directory 要搜索的目录
     * @param extension 文件扩展名（不含点号）
     * @return 找到的文件列表
     */
    private List<File> searchFilesInDirectory(File directory, String extension) {
        List<File> result = new ArrayList<>();
        
        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory()) {
            return result;
        }
        
        // 获取目录中的所有文件和子目录
        File[] files = directory.listFiles();
        if (files == null) {
            return result;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                // 如果是目录，则递归搜索
                result.addAll(searchFilesInDirectory(file, extension));
            } else if (file.isFile() && file.getName().endsWith("." + extension)) {
                // 如果是具有指定扩展名的文件，则添加到结果中
                result.add(file);
            }
        }
        
        return result;
    }
}
