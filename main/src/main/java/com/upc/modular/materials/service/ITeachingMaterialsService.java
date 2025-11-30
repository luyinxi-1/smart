package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsPageSearchDto;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsSaveOrUpdateParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookNameMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
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

   TeachingMaterials insertMaterials(TeachingMaterialsSaveOrUpdateParam param);
    void getFileMaterials(Long id, Integer imageSetId, Long textbookId, String action, HttpServletResponse response);

    String getLinkMaterials(Long id, Long textbookId);

    Page<TeachingMaterialsReturnVo> getPage(TeachingMaterialsPageSearchDto param);

    TeachingMaterialsReturnVo getTeachingMaterials(Long id, Long textbookId);

   List<TeachingMaterials> getMaterialsByTextbookId(Long textbookId, String materialName);

    String updateTeachingMaterialsById(TeachingMaterialsSaveOrUpdateParam param);

    void deleteTeachingMaterialsByIds(List<Long> ids);

    MaterialsTextbookNameMappingReturnParam getMaterialsTextbookMappingByMaterialsId(List<Long> ids);
}
