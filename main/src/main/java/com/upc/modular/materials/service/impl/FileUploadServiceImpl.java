package com.upc.modular.materials.service.impl;

import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.common.utils.FileManageUtil;
// import com.upc.common.utils.UserUtils; // <-- 不再需要，可以移除
import com.upc.modular.materials.service.IFileUploadService;
import com.upc.modular.materials.controller.param.dto.FileUploadResultDTO;
import com.upc.modular.materials.entity.TeachingMaterials;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileUploadServiceImpl implements IFileUploadService {

    // 假设 basePath 是您在类中定义的上传根目录，例如: @Value("${file.upload-path}")
    private final String basePath = "upload";

    @Override
    public String uploadMaterialFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "上传文件不能为空");
        }
      /*  // 将 "ppt","word", "excel", "pdf" 统一映射为 "file" 类型
        String processedType = type;
        if ("word".equalsIgnoreCase(type) || "excel".equalsIgnoreCase(type) || "pdf".equalsIgnoreCase(type)||"ppt".equalsIgnoreCase(type)) {
            processedType = "file";
        }*/
        // 校验素材类型是否受支持 (假设您有一个支持的类型列表)
        // 注意：这里不再需要判断 "imageSet" 和 "link"
        if (!TeachingMaterials.SUPPORTED_TYPES.contains(type)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "不支持该素材类型: " + type);
        }
        // 2. 构建文件存储路径
        // 路径结构： basePath/teaching_materials/{type}/{yyyyMMdd}
        // 这里将使用处理后的 processedType ("file") 来创建文件夹
        Path folderPath = Paths.get(basePath, "teaching_materials", type, FileManageUtil.yyyyMMddStr());
        String finalPath;
/*
        // 3. 生成唯一文件名
        String fileName = FileManageUtil.createFileName(file);

        // 4. 执行文件上传
        String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);*/
        try {
            // 核心逻辑变更：判断 type 是否为 "simulation"
            if ("simulation".equalsIgnoreCase(type)) {
                // --- 'simulation' 类型的处理流程 ---

                // 验证：确保为 simulation 类型上传的是一个ZIP文件
                if (!isZipFile(file)) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "simulation类型必须上传ZIP压缩包");
                }

                // 步骤 1: 将原始ZIP包作为一个普通文件保存下来
                String originalFileName = FileManageUtil.createFileName(file);
                String savedArchivePath = FileManageUtil.uploadFile(file, folderPath, originalFileName);

                if (ObjectUtils.isEmpty(savedArchivePath)) {
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "保存simulation压缩包失败");
                }

                File savedZipFile = new File(savedArchivePath);

                // --- 主要修改点在这里 ---
                // 步骤 2: 创建一个专属的子目录用于存放解压内容
                // 目录名 = 保存后的文件名去掉 .zip 后缀
                String savedFileName = savedZipFile.getName();
                String unzipFolderName = savedFileName.substring(0, savedFileName.lastIndexOf('.'));
                Path unzipDestPath = folderPath.resolve(unzipFolderName); // 例如: .../20251118/一串唯一的UUID/

                // 步骤 3: 将ZIP解压到这个新的专属子目录中
                unzip(savedZipFile, unzipDestPath);

                // 结果返回这个新的、更精确的解压子目录的路径
                finalPath = unzipDestPath.toString();

            } else {
                // --- 其他类型（如 word, ppt 等）的处理流程，保持不变 ---
                String fileName = FileManageUtil.createFileName(file);
                finalPath = FileManageUtil.uploadFile(file, folderPath, fileName);
            }
        } catch (IOException e) {
            // e.printStackTrace(); // 生产环境建议使用日志框架
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "文件处理失败: " + e.getMessage());
        }

        if (ObjectUtils.isEmpty(finalPath)) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "文件处理失败");
        }

        // 统一返回路径分隔符为'/'
        return finalPath.replace(File.separator, "/");
    }
    /**
     * 判断文件是否为ZIP文件
     * 通过MIME类型和文件后缀名双重判断，提高准确性
     */
    private boolean isZipFile(MultipartFile file) {
        return "application/zip".equals(file.getContentType()) ||
                (file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".zip"));
    }

    /**
     * 将ZIP文件解压到目标路径
     * @param zipFile 要解压的ZIP文件
     * @param destDirectory 解压的目标目录
     */
    private void unzip(File zipFile, Path destDirectory) throws IOException {
        // 从已保存的File对象创建FileInputStream
        try (InputStream fis = new FileInputStream(zipFile);
             ZipInputStream zipIn = new ZipInputStream(fis)) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                Path filePath = destDirectory.resolve(entry.getName());

                // 安全性检查：防止 "Zip Slip" 路径遍历攻击
                if (!filePath.toAbsolutePath().startsWith(destDirectory.toAbsolutePath())) {
                    throw new IOException("解压文件失败：检测到非法路径 " + entry.getName());
                }

                if (!entry.isDirectory()) {
                    // 确保父级目录存在
                    if (!filePath.getParent().toFile().exists()){
                        filePath.getParent().toFile().mkdirs();
                    }
                    extractFile(zipIn, filePath);
                } else {
                    Files.createDirectories(filePath);
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    /**
     * 从ZipInputStream中提取文件内容并写入
     */
    private void extractFile(ZipInputStream zipIn, Path filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = zipIn.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
}
