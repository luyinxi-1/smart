package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingDto;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mjh
 * @since 2025-08-23
 */
public interface IMaterialsTextbookMappingService extends IService<MaterialsTextbookMapping> {

    Long insertMapping(Long textbookId, Long materialId, String chapterName, String chapterId);
    /**
     * 批量添加多条独立的教材与素材的映射关系
     *
     * @param mappings 包含多个映射关系信息的DTO列表
     * @return 新增映射记录的ID列表
     */
    List<Long> insertMappingBatch(List<MaterialsTextbookMappingDto> mappings);
    Page<MaterialsTextbookMappingReturnParam> getPage(MaterialsTextbookMappingPageSearchParam param);
    TeachingMaterialsReturnVo getMaterialsByMappingId(Long id);
}