package com.upc.modular.materials.service.impl;

import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.common.utils.FileManageUtil;
import com.upc.common.utils.UserUtils;
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
    public FileUploadResultDTO uploadMaterialFiles(List<MultipartFile> files, Boolean isPublic, String type) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "上传文件不能为空");
        }
        List<String> savedFilePaths = new ArrayList<>();
        String finalDirectoryPath = "";
        long totalFilesSize = 0L;

        if ("imageSet".equals(type)) {
            Path folderPath;
            String imageSetLength = String.valueOf(files.size());
            String imageSetName = UUID.randomUUID() + "_" + imageSetLength;
            if (isPublic)
                folderPath = Paths.get("upload", "teaching_materials", "public",
                        "imageSet", FileManageUtil.yyyyMMddStr(), imageSetName);
            else
                folderPath = Paths.get("upload", "teaching_materials", "private",
                        UserUtils.get().getId().toString(),
                        "imageSet", FileManageUtil.yyyyMMddStr(), imageSetName);

            finalDirectoryPath = folderPath.toString();

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String fileName = FileManageUtil.createFileName(file, String.valueOf(i + 1));
                String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);
                if (ObjectUtils.isEmpty(filePath))
                    throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "上传失败");
                savedFilePaths.add(filePath);
                totalFilesSize += file.getSize();
            }

        } else if (!"link".equals(type)) {
            if (!TeachingMaterials.SUPPORTED_TYPES.contains(type))
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "不支持该素材类型");
            if (files.size() != 1)
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该类型只支持单文件上传");

            Path folderPath;
            if (isPublic)
                folderPath = Paths.get("upload", "teaching_materials", "public",
                        type, FileManageUtil.yyyyMMddStr());
            else
                folderPath = Paths.get("upload", "teaching_materials", "private",
                        UserUtils.get().getId().toString(),
                        type, FileManageUtil.yyyyMMddStr());

            MultipartFile file = files.get(0);
            String fileName = FileManageUtil.createFileName(file);
            String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);
            if (ObjectUtils.isEmpty(filePath))
                throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "上传失败");

            savedFilePaths.add(filePath);
            totalFilesSize += file.getSize();
        }

        FileUploadResultDTO result = new FileUploadResultDTO();
        result.setFilePaths(savedFilePaths);
        result.setDirectoryPath(finalDirectoryPath);
        result.setTotalSizeMB(Math.round(totalFilesSize / (1024.0 * 1024.0) * 100) / 100.0);

        return result; // <-- Service直接返回处理结果DTO
    }
}
