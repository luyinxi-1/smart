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
import java.util.UUID;

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
}
