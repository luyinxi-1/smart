package com.upc.modular.auth.controller;

import com.upc.common.responseparam.R;
import com.upc.common.utils.FileManageUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //文件名处理
        String originalFilename = file.getOriginalFilename();
        //suffix为图片后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID生成新的文件名,防止传入的文件名因重复而覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        String filePath = basePath + "/" + FileManageUtil.yyyyMMddStr();

        //创建目录
        File dir = new File(filePath);
        //如果该目录不存在,就创建出来
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            //将临时图片转存
            file.transferTo(new File(dir.getAbsolutePath(),fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //log.info(file.toString());

        return R.ok(filePath + "/" + fileName);
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response
     */
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
}
