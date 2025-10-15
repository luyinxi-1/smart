package com.upc.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @Author: xth
 * @Date: 2025/9/8 11:10
 */
@Service
@Slf4j
public class Base64Decode {

    public static boolean GenerateImage(String imgStr, String imgFilePath) {
        if (imgStr == null || imgStr.isEmpty()) { // 增加一个空字符串检查
            log.warn("传入的图像数据为空 (imgStr is null or empty)");
            return false;
        }

        try {
            // Base64解码
            // 注意：这里的 Base64.decodeBase64 来自于 Apache Commons Codec 库
            byte[] imageBytes = Base64.decodeBase64(imgStr);

            // 确保父目录存在 (您原来的这部分代码是正确的，予以保留)
            File imageFile = new File(imgFilePath);
            File parentDir = imageFile.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    log.error("创建父目录失败: {}", parentDir.getAbsolutePath());
                    return false;
                }
            }

            // 使用 try-with-resources 语句，可以自动关闭流，更安全
            try (OutputStream out = new FileOutputStream(imageFile)) {
                out.write(imageBytes);
                out.flush();
            } // out 会在这里被自动关闭

            log.info("成功生成图片: {}", imgFilePath);
            return true;

        } catch (IllegalArgumentException e) {
            // --- 【关键修改】--- 捕获具体的异常并打印日志！
            log.error("Base64解码失败！请检查传入的imgStr是否为有效的Base64编码。传入的字符串前50位: '{}'",
                    imgStr.substring(0, Math.min(imgStr.length(), 50)), e);
            return false;
        } catch (Exception e) {
            // --- 【关键修改】--- 捕获所有其他异常并打印日志！
            log.error("生成图片时发生未知错误！路径: '{}'", imgFilePath, e);
            return false;
        }
    }

}
