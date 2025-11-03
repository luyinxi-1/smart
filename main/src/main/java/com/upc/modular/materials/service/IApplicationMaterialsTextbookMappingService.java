package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsTextbookMappingDto;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.entity.ApplicationMaterialsTextbookMapping;

import java.util.List;

/**
 * <p>
 * 应用素材与教材关联服务类
 * </p>
 *
 * @author system
 * @since 2025-10-31
 */
public interface IApplicationMaterialsTextbookMappingService extends IService<ApplicationMaterialsTextbookMapping> {

    /**
     * 添加教材与应用素材的单个映射关系
     *
     * @param textbookId            教材ID
     * @param applicationMaterialId 应用素材ID
     * @param chapterName           章节名称
     * @param chapterId             章节ID
     * @param chapterUuid           章节UUID
     * @return 新增映射记录的ID
     */
    Long insertMapping(Long textbookId, Long applicationMaterialId, String chapterName, Long chapterId, String chapterUuid);

    /**
     * 批量添加多条独立的教材与应用素材的映射关系
     * 注意：即使mappings为空数组，也会删除该教材下所有旧的绑定关系
     *
     * @param textbookId 教材ID
     * @param mappings 包含多个映射关系信息的DTO列表
     * @return 新增映射记录的ID列表
     */
    List<Long> insertMappingBatch(Long textbookId, List<ApplicationMaterialsTextbookMappingDto> mappings);

    /**
     * 分页查询应用素材教材绑定
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<ApplicationMaterialsTextbookMappingReturnParam> getPage(ApplicationMaterialsTextbookMappingPageSearchParam param);
}

