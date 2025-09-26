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

    // 假设 basePath 是您在类中定义的上传根目录，例如: @Value("${file.upload-path}")
    private final String basePath = "upload";

    @Override
    public String uploadMaterialFile(MultipartFile file, String type) {
        // 1. 输入参数校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "上传文件不能为空");
        }

        // 校验素材类型是否受支持 (假设您有一个支持的类型列表)
        // 注意：这里不再需要判断 "imageSet" 和 "link"
        if (!TeachingMaterials.SUPPORTED_TYPES.contains(type)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "不支持该素材类型: " + type);
        }

        // 2. 构建文件存储路径
        // 路径结构： basePath/teaching_materials/{type}/{yyyyMMdd}
        Path folderPath = Paths.get(basePath, "teaching_materials", type, FileManageUtil.yyyyMMddStr());

        // 3. 生成唯一文件名
        String fileName = FileManageUtil.createFileName(file);

        // 4. 执行文件上传（假设 FileManageUtil.uploadFile 是您工具类中的方法）
        // public static String uploadFile(MultipartFile file, Path folderPath, String fileName) { ... }
        String filePath = FileManageUtil.uploadFile(file, folderPath, fileName);

        // 5. 结果处理和返回
        if (ObjectUtils.isEmpty(filePath)) {
            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "文件上传失败");
        }

        return filePath;
    }
}
