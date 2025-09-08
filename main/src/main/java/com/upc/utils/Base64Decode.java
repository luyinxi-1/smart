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

    public static boolean GenerateImage(String imgStr, String imgFilePath) {  //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return false;

        // 创建文件对象
        File file = new File(imgFilePath);
        // 获取文件所在目录
        File parentDir = file.getParentFile();
        // 如果父目录不存在，则创建父目录
        if (!parentDir.exists()) {
            boolean dirsCreated = parentDir.mkdirs();
            if (dirsCreated) {
                log.info("文件夹不存在，已成功创建父目录");
            } else {
                log.info("无法创建父目录");
            }
        }

        try {
            //Base64解码
            byte[] b = Base64.decodeBase64(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }
            //生成jpeg图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
