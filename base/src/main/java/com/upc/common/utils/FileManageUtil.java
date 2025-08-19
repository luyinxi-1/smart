package com.upc.common.utils;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class FileManageUtil {

    /**
     * 上传文件并保存到 ./uplpad 下的指定的目录
     * 该方法会对上传的文件进行类型验证，确保文件的内容类型符合要求，
     * 并且检查目标路径是否有效。文件将被保存到指定目录下，并返回文件的保存路径。
     *
     * @param file
     * @param parentPath 保存文件的父目录
     * @return
     */
    public static String uploadFile(MultipartFile file, Path parentPath) {
        // 验证文件类型
        String contentType = file.getContentType();
        if (!FileType.isValidFileType(contentType))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，只能上传指定的文件类型");

        if (ObjectUtils.isEmpty(parentPath) || parentPath.isAbsolute() || parentPath.startsWith("/") || parentPath.startsWith("\\"))
            throw new RuntimeException("请指定正确的保存路径");
        parentPath = parentPath.normalize();

        if (!parentPath.toString().startsWith("upload"))
            parentPath = Paths.get("upload").resolve(parentPath);

        String originalFileName = file.getOriginalFilename();
        if (ObjectUtils.isEmpty(originalFileName))
            throw new RuntimeException("文件名获取失败");

        String fileName = randomStr();
        if (originalFileName.contains(".")) {
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            fileName += fileExtension;
        } else
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，只能上传指定的文件类型");

        File Filefiled = new File(parentPath.toString());
        if (!Filefiled.exists())
            if (!Filefiled.mkdirs())
                throw new RuntimeException("创建目录失败");
        try {
            file.transferTo(new File(Filefiled.getAbsolutePath(), fileName));   // 将上传的文件保存到指定路径
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败");
        }

        return parentPath.resolve(fileName).toString();
    }


    /**
     * 服务器上移动文件，目标路径文件已存在时报错
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    public static Boolean moveFile(String oldPath, String newPath) {
        if (!newPath.startsWith("upload/"))
            newPath = "upload/" + newPath;
        Path sourcePath = Paths.get(oldPath);
        Path destinationPath = Paths.get(newPath);

        if (!Files.exists(sourcePath.getParent())) {
            System.err.println("源文件所在目录不存在：" + oldPath);
            return false;
        }
        if (!Files.exists(sourcePath)) {
            System.err.println("源文件不存在：" + oldPath);
            return false;
        }
        if (Files.exists(destinationPath)) {
            System.err.println("目标文件已存在：" + newPath);
            return false;
        }
        if (sourcePath.equals(destinationPath)) {
            System.err.println("源文件和目标文件相同：" + oldPath + " -> " + newPath);
            return false;
        }
        File destinationFilefiled = new File(destinationPath.getParent().toString());
        if (!destinationFilefiled.exists()) {
            if (!destinationFilefiled.mkdirs())
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "创建目录失败");
        }

        try {
            // 移动文件并复制文件属性
            Files.move(sourcePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            System.err.println("文件移动失败：" + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 服务器上复制文件，目标路径文件已存在时报错
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    public static Boolean copyFile(String oldPath, String newPath) {
        if (!newPath.startsWith("upload/"))
            newPath = "upload/" + newPath;
        Path sourcePath = Paths.get(oldPath);
        Path destinationPath = Paths.get(newPath);

        if (!Files.exists(sourcePath.getParent())) {
            System.err.println("源文件所在目录不存在：" + oldPath);
            return false;
        }
        if (!Files.exists(sourcePath)) {
            System.err.println("源文件不存在：" + oldPath);
            return false;
        }
        if (Files.exists(destinationPath)) {
            System.err.println("目标文件已存在：" + newPath);
            return false;
        }
        if (sourcePath.equals(destinationPath)) {
            System.err.println("源文件和目标文件相同：" + oldPath + " -> " + newPath);
            return false;
        }
        File destinationFilefiled = new File(destinationPath.getParent().toString());
        if (!destinationFilefiled.exists()) {
            if (!destinationFilefiled.mkdirs())
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "创建目录失败");
        }

        try {
            // 复制文件并复制文件属性
            Files.copy(sourcePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            System.err.println("文件复制失败：" + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 删除文件，成功返回true，失败返回false
     *
     * @param pathIncludeFileName 路径名（包含文件名）
     * @return 是否删除成功
     * @author 秋天
     */
    public static Boolean deleteFile(String pathIncludeFileName) {
        File file = new File(pathIncludeFileName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static String yyyyMMddStr() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(now);
    }

    public static String randomStr() {
        return System.currentTimeMillis() + String.valueOf(ThreadLocalRandom.current().nextInt(1, 1001));
    }
}