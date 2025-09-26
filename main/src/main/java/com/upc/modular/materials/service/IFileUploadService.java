package com.upc.modular.materials.service;
import com.upc.modular.materials.controller.param.dto.FileUploadResultDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IFileUploadService {

    /**
     * 上传单个教学素材文件
     *
     * @param file 上传的文件
     * @param type 素材类型
     * @return 文件保存的相对路径
     */
    String uploadMaterialFile(MultipartFile file, String type);
}