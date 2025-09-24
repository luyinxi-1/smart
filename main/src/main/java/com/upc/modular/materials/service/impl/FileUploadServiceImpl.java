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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements IFileUploadService {

    @Override
    // 1. 更新方法签名，移除 isPublic
    public FileUploadResultDTO uploadMaterialFiles(List<MultipartFile> files, String type) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "上传文件不能为空");
        }

        List<String> savedFilePaths = new ArrayList<>();
        String finalDirectoryPath = "";
        long totalFilesSize = 0L;

        // 2. 定义一个统一的基础路径，不再区分 public/private
        Path basePath = Paths.get("upload", "teaching_materials", type, FileManageUtil.yyyyMMddStr());
        Path folderPath; // 最终用于保存文件的目录

        if ("imageSet".equals(type)) {
            // 对于图集，在基础路径下再创建一个唯一的子目录
            String imageSetName = UUID.randomUUID() + "_" + files.size();
            folderPath = basePath.resolve(imageSetName); // resolve 用于拼接路径
            finalDirectoryPath = folderPath.toString().replace("\\", "/");

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                totalFilesSize += file.getSize();
                // 文件名使用序号 (1, 2, 3...)
                String fileName = FileManageUtil.createFileName(file, String.valueOf(i + 1));
                String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);

                if (ObjectUtils.isEmpty(filePath))
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "上传失败");
                savedFilePaths.add(filePath);
            }

        } else if (!"link".equals(type)) {
            // 对于其他单文件类型
            if (!TeachingMaterials.SUPPORTED_TYPES.contains(type))
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "不支持该素材类型");
            if (files.size() != 1)
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该类型只支持单文件上传");

            // 直接使用基础路径作为保存目录
            folderPath = basePath;

            MultipartFile file = files.get(0);
            totalFilesSize += file.getSize();
            // 文件名使用UUID
            String fileName = FileManageUtil.createFileName(file);
            String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);

            if (ObjectUtils.isEmpty(filePath))
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "上传失败");
            savedFilePaths.add(filePath);
        }

        FileUploadResultDTO result = new FileUploadResultDTO();
        result.setFilePaths(savedFilePaths);
        result.setDirectoryPath(finalDirectoryPath);
        // 计算总大小的逻辑保持不变
        result.setTotalSizeMB(Math.round(totalFilesSize / (1024.0 * 1024.0) * 100) / 100.0);

        return result;
    }
}