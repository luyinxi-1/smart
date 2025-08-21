package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.materials.entity.TeachingMaterials;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mjh
 * @since 2025-07-17
 */
public interface ITeachingMaterialsService extends IService<TeachingMaterials> {

    String insertMaterials(MultipartFile multipartFile, TeachingMaterials teachingMaterials);

    void downloadMaterials(Long fileId, String fileName, HttpServletResponse response);
}
