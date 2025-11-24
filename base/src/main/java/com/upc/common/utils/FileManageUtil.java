package com.upc.common.utils;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class FileManageUtil {

    /**
     * 上传文件并保存到 ./uplpad 下的指定的目录
     * 该方法会对上传的文件进行类型验证，确保文件的内容类型符合要求(必须有扩展名)，
     * 并且检查目标路径是否有效。文件将被保存到指定目录下，并返回文件的保存路径。
     * 保存文件的目录标准：upload/xxxx
     *
     * @param file
     * @param folderPath 保存文件的目录，标准：upload/xxxx
     * @return 保存的文件路径
     */
    public static String uploadFile(MultipartFile file, Path folderPath, String fileName) {
        // 验证文件类型
//        if (!FileType.isValidFileType(file))
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，只能上传指定的文件类型");
        // 验证目标路径
        if (folderPath == null || folderPath.toString().trim().isEmpty())
            throw new RuntimeException("请指定正确的保存路径");
        folderPath = folderPath.normalize();
        if (!folderPath.startsWith("upload") || folderPath.isAbsolute())
            throw new RuntimeException("请指定正确的保存路径");
        // 验证文件名
        if (!fileName.contains(".") || fileName.lastIndexOf(".") == fileName.length() - 1)
            throw new RuntimeException("需要文件扩展名");

        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，文件保存失败");
        }
        Path filePath = folderPath.resolve(fileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，文件保存失败");
        }

        return filePath.toString().replace("\\", "/");
    }

    /**
     * 下载/在线浏览 文件中，执行文件传输
     * 提前设置好响应头，再执行文件传输！！！
     */
    public static void transferFile(Path filePath, HttpServletResponse response) {
        // 1. 校验文件是否存在
        if (filePath == null || !Files.exists(filePath)) {
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "，文件不存在");
        }

        if (Files.isDirectory(filePath)) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，不能下载目录");
        }

        try (
                // 2. 使用 Files.newInputStream() 替代 FileInputStream
                InputStream in = Files.newInputStream(filePath);
                BufferedInputStream bis = new BufferedInputStream(in);

                // 3. 获取响应输出流
                OutputStream out = response.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(out)
        ) {

            byte[] buffer = new byte[1024 * 8]; // 8KB 缓冲区
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush(); // flush 可省略，因为 try-with-resources 会自动 flush

        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，文件传输失败");
        }
    }

    /**
     * 保存Base64图片
     *
     * @param base64Data base64字符串（可包含前缀：data:image/png;base64,xxx）
     * @param folderPath 保存目录
     * @param fileName   保存文件名
     * @return 图片完整路径
     */
    public static String saveBase64Image(String base64Data, Path folderPath, String fileName) {
        if (ObjectUtils.isEmpty(base64Data))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，请上传有效的图片");
        if (!fileName.endsWith(".png"))
            throw new RuntimeException("文件名错误");

        if (base64Data.contains(","))
            base64Data = base64Data.split(",")[1];

        try {
            byte[] bytes = Base64.getDecoder().decode(base64Data);

            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName);

            Files.write(filePath, bytes);

            return filePath.toString().replace("\\", "/");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "，图片保存失败");
        }
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
        /*File destinationFilefiled = new File(destinationPath.getParent().toString());
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
        return true;*/
        File destinationFilefiled = new File(destinationPath.getParent().toString());
        if (!destinationFilefiled.exists()) {
            if (!destinationFilefiled.mkdirs())
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "创建目录失败");
        }

        try {
            // 确保目标目录存在
            Files.createDirectories(destinationPath.getParent());

            // 移动文件，如果目标文件存在则替换（使用兼容的选项）
            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("文件移动失败：" + e.getMessage());
            e.printStackTrace();
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
     * 创建文件名，默认使用UUID
     *
     * @param file
     * @return
     */
    public static String createFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (ObjectUtils.isEmpty(originalFileName))
            throw new RuntimeException("文件名获取失败");

        String fileName = UUID.randomUUID().toString();
        if (originalFileName.contains(".")) {
            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            fileName += suffix;
        }
        return fileName;
    }

    /**
     * 创建带有后缀的自定义文件名
     *
     * @param file
     * @param fileNameNoSuf
     * @return
     */
    public static String createFileName(MultipartFile file, String fileNameNoSuf) {
        String originalFileName = file.getOriginalFilename();
        if (ObjectUtils.isEmpty(originalFileName))
            throw new RuntimeException("文件名获取失败");

        if (originalFileName.contains(".")) {
            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            fileNameNoSuf += suffix;
        }
        return fileNameNoSuf;
    }

    public static String yyyyMMddStr() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(now);
    }
}