package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingDto;

import java.util.ArrayList;
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

    Page<MaterialsTextbookMappingReturnParam> getPage(MaterialsTextbookMappingPageSearchParam param);

    Long insertMapping(Long textbookId, Long materialId, String chapterName, Long chapterId, String chapterUuid);

    List<Long> insertMappingBatch(List<MaterialsTextbookMappingDto> mappings);
    
    List<Long> insertMappingBatchByChapters(Long textbookId, List<Long> chapterIds, List<MaterialsTextbookMappingDto> mappings);

    TeachingMaterialsReturnVo getMaterialsByMappingId(Long id);
    
    /**
     * 根据章节ID列表删除教材与素材的绑定关系
     * @param textbookId 教材ID
     * @param chapterIds 章节ID列表
     */
    void removeMappingsByChapterIds(Long textbookId, List<Long> chapterIds);
    
    /**
     * 根据章节ID获取教材ID
     * @param chapterId 章节ID
     * @return 教材ID
     */
    Long getTextbookIdByChapterId(Long chapterId);
}