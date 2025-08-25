package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.materials.entity.TeachingMaterials;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mjh
 * @since 2025-07-17
 */
public interface ITeachingMaterialsService extends IService<TeachingMaterials> {

    String insertFileMaterials(MultipartFile multipartFile, TeachingMaterials teachingMaterials);

    void getFileMaterials(String fileName, Long textbookId, String action, HttpServletResponse response);

    String insertLinkMaterials(TeachingMaterials teachingMaterials);

    String getLinkMaterials(String fileName, Long textbookId);

    String insertPictureMaterials(List<MultipartFile> multipartFile, TeachingMaterials teachingMaterials);

    void getOnePictureMaterials(String fileName, Long textbookId, String action, HttpServletResponse response);
}
