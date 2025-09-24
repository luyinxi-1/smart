package com.upc.modular.materials.service;
import com.upc.modular.materials.controller.param.dto.FileUploadResultDTO; // 引入我们之前定义的DTO
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IFileUploadService {

    /**
     * 上传教学素材文件
     *
     * @param files    上传的文件列表
     * @param isPublic 是否公开
     * @param type     素材类型
     * @return 包含文件路径和大小等信息的 DTO
     */
    FileUploadResultDTO uploadMaterialFiles(List<MultipartFile> files, Boolean isPublic, String type);

}