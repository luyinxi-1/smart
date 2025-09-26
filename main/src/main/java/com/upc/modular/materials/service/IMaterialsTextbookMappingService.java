package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mjh
 * @since 2025-08-23
 */
public interface IMaterialsTextbookMappingService extends IService<MaterialsTextbookMapping> {

    Long insertMapping(Long textbookId, Long materialId, String chapterName, Integer chapterId);

    Page<MaterialsTextbookMappingReturnParam> getPage(MaterialsTextbookMappingPageSearchParam param);
    TeachingMaterialsReturnVo getMaterialsByMappingId(Long id);
}
