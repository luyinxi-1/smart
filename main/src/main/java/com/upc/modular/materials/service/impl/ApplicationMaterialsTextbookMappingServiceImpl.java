package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsTextbookMappingDto;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.entity.ApplicationMaterials;
import com.upc.modular.materials.entity.ApplicationMaterialsTextbookMapping;
import com.upc.modular.materials.mapper.ApplicationMaterialsTextbookMappingMapper;
import com.upc.modular.materials.service.IApplicationMaterialsTextbookMappingService;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 应用素材与教材关联服务实现类
 * </p>
 *
 * @author system
 * @since 2025-10-31
 */
@Service
public class ApplicationMaterialsTextbookMappingServiceImpl extends ServiceImpl<ApplicationMaterialsTextbookMappingMapper, ApplicationMaterialsTextbookMapping> implements IApplicationMaterialsTextbookMappingService {

    @Autowired
    private ApplicationMaterialsTextbookMappingMapper baseMapper;

    @Autowired
    private TextbookMapper textbookMapper;

    @Resource
    private TextbookCatalogMapper textbookCatalogMapper;
    
    @Resource
    private com.upc.modular.materials.mapper.ApplicationMaterialsMapper applicationMaterialsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertMapping(Long textbookId, Long applicationMaterialId, String chapterName, Long chapterId, String chapterUuid) {
        // 1. 基础参数校验
        if (ObjectUtils.isEmpty(textbookId) || ObjectUtils.isEmpty(applicationMaterialId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID和应用素材ID不能为空");
        }

        // 2. 解析章节ID
        Long finalChapterId = chapterId; // 优先使用传入的 chapterId

        // 如果 chapterId 未提供，但 chapterUuid 提供了，则进行查询转换
        if (finalChapterId == null && chapterUuid != null && !chapterUuid.trim().isEmpty()) {
            LambdaQueryWrapper<TextbookCatalog> catalogQuery = new LambdaQueryWrapper<TextbookCatalog>()
                    .eq(TextbookCatalog::getCatalogUuid, chapterUuid)
                    .select(TextbookCatalog::getId); // 仅查询ID字段以提高性能

            TextbookCatalog textbookCatalog = textbookCatalogMapper.selectOne(catalogQuery);

            if (textbookCatalog == null) {
                // 如果根据UUID找不到章节，则抛出异常
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "无效的章节UUID: " + chapterUuid);
            }
            finalChapterId = textbookCatalog.getId(); // 将查询到的ID赋值给 finalChapterId
        }

        // 3. 最终校验：确保章节ID、章节名称已确定
        if (finalChapterId == null || ObjectUtils.isEmpty(chapterName)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "章节ID或章节名称不能为空");
        }

        // 4. 唯一性校验：检查该应用素材是否已被绑定
        LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, applicationMaterialId);
        ApplicationMaterialsTextbookMapping existingMapping = this.getOne(queryWrapper);
        if (existingMapping != null) {
            String errorMessage = String.format(
                    "该应用素材 (ID: %d) 已被绑定到教材 (ID: %d) 的章节 '%s', ID为 '%d'。一个应用素材只能绑定一次。",
                    applicationMaterialId,
                    existingMapping.getTextbookId(),
                    existingMapping.getChapterName(),
                    existingMapping.getChapterId()
            );
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, errorMessage);
        }

        // 5. 如果校验通过，则创建并保存新的绑定关系
        // 获取当前登录用户
        Long currentUserId = com.upc.common.utils.UserUtils.get() != null ? 
            com.upc.common.utils.UserUtils.get().getId() : null;
        
        ApplicationMaterialsTextbookMapping mapping = new ApplicationMaterialsTextbookMapping();
        mapping.setTextbookId(textbookId);
        mapping.setApplicationMaterialId(applicationMaterialId);
        mapping.setChapterName(chapterName);
        mapping.setChapterId(finalChapterId); // 使用最终确定的章节ID
        mapping.setCreator(currentUserId); // 设置创建人
        mapping.setOperator(currentUserId); // 设置操作人

        if (this.save(mapping)) {
            return mapping.getId();
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> insertMappingBatch(Long textbookId, List<ApplicationMaterialsTextbookMappingDto> mappings) {
        // 1. 基本参数校验
        // 注意：即使mappings为空，也需要执行删除和清空操作
        if (textbookId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        // 如果mappings为空，直接删除该教材下所有绑定关系
        if (CollectionUtils.isEmpty(mappings)) {
            // 删除该教材下所有旧的绑定关系
            this.remove(new LambdaQueryWrapper<ApplicationMaterialsTextbookMapping>()
                    .eq(ApplicationMaterialsTextbookMapping::getTextbookId, textbookId));
            
            return Collections.emptyList();
        }

        // 1.5 如果传入了 chapterUuid，则通过它查询并填充 chapterId
        // 筛选出所有需要通过 UUID 解析 chapterId 的 DTO
        List<String> uuidsToResolve = mappings.stream()
                .filter(dto -> dto.getChapterId() == null && dto.getChapterUuid() != null && !dto.getChapterUuid().trim().isEmpty())
                .map(ApplicationMaterialsTextbookMappingDto::getChapterUuid)
                .distinct()
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(uuidsToResolve)) {
            // 批量查询对应的章节目录信息
            List<TextbookCatalog> catalogs = textbookCatalogMapper.selectList(
                    new LambdaQueryWrapper<TextbookCatalog>()
                            .in(TextbookCatalog::getCatalogUuid, uuidsToResolve)
            );
            // 创建一个 UUID -> ID 的映射，方便快速查找
            Map<String, Long> uuidToIdMap = catalogs.stream()
                    .collect(Collectors.toMap(TextbookCatalog::getCatalogUuid, TextbookCatalog::getId));
            // 检查是否有UUID未找到对应的目录
            if (uuidToIdMap.size() != uuidsToResolve.size()) {
                String notFoundUuids = uuidsToResolve.stream()
                        .filter(uuid -> !uuidToIdMap.containsKey(uuid))
                        .collect(Collectors.joining(", "));
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "以下章节UUID无效或不存在: " + notFoundUuids);
            }
            // 回填 chapterId到DTO中
            for (ApplicationMaterialsTextbookMappingDto dto : mappings) {
                if (dto.getChapterId() == null && dto.getChapterUuid() != null) {
                    Long resolvedId = uuidToIdMap.get(dto.getChapterUuid());
                    if (resolvedId == null) {
                        // 兜底校验，理论上不会进入此分支
                        throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                                "未能找到章节UUID: " + dto.getChapterUuid() + " 对应的章节ID");
                    }
                    dto.setChapterId(resolvedId);
                }
            }
        }

        // 2. 【前置校验】
        // 2.1 检查请求列表内部是否存在重复的应用素材ID
        Set<Long> uniqueApplicationMaterialIdsInRequest = new HashSet<>();
        for (ApplicationMaterialsTextbookMappingDto dto : mappings) {
            if (dto.getApplicationMaterialId() == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "请求参数中存在空的应用素材ID");
            }
            if (!uniqueApplicationMaterialIdsInRequest.add(dto.getApplicationMaterialId())) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "请求参数中存在重复的应用素材ID: " + dto.getApplicationMaterialId());
            }
        }

        // 2.2 校验教材ID是否存在
        if (textbookMapper.selectById(textbookId) == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不存在，请检查后重试！");
        }

        // 2.3 批量校验所有涉及的应用素材ID是否存在
        Set<Long> applicationMaterialIds = mappings.stream().map(ApplicationMaterialsTextbookMappingDto::getApplicationMaterialId).collect(Collectors.toSet());
        if (!applicationMaterialIds.isEmpty()) {
            long existingApplicationMaterialCount = applicationMaterialsMapper.selectCount(
                    new LambdaQueryWrapper<ApplicationMaterials>().in(ApplicationMaterials::getId, applicationMaterialIds));
            if (existingApplicationMaterialCount != applicationMaterialIds.size()) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "部分应用素材ID不存在，请检查后重试！");
            }
        }

        // 3. 【执行删除】删除该教材下所有旧的绑定关系
        this.remove(new LambdaQueryWrapper<ApplicationMaterialsTextbookMapping>()
                .eq(ApplicationMaterialsTextbookMapping::getTextbookId, textbookId));

        // 4. 【最终唯一性校验】
        // 检查 application_material_id
        LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> conflictCheckWrapper = new LambdaQueryWrapper<>();
        conflictCheckWrapper.in(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, uniqueApplicationMaterialIdsInRequest);

        List<ApplicationMaterialsTextbookMapping> conflictingBindings = this.list(conflictCheckWrapper);
        if (!conflictingBindings.isEmpty()) {
            String existingDetails = conflictingBindings.stream()
                    .map(e -> String.format("应用素材ID:%d，已被教材ID:%d，章节'%s'，章节ID：%d绑定", 
                            e.getApplicationMaterialId(), e.getTextbookId(), e.getChapterName(), e.getChapterId()))
                    .collect(Collectors.joining("; "));

            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "操作失败，部分应用素材已绑定到本书的其他章节: " + existingDetails);
        }

        // 5. 【执行新增】
        // 获取当前登录用户
        Long currentUserId = com.upc.common.utils.UserUtils.get() != null ? 
            com.upc.common.utils.UserUtils.get().getId() : null;
        
        // 数据转换与批量插入
        List<ApplicationMaterialsTextbookMapping> entitiesToInsert = mappings.stream().map(dto -> {
            ApplicationMaterialsTextbookMapping entity = new ApplicationMaterialsTextbookMapping();
            entity.setTextbookId(textbookId); // 使用参数传入的教材ID
            entity.setApplicationMaterialId(dto.getApplicationMaterialId());
            entity.setChapterName(dto.getChapterName());
            entity.setChapterId(dto.getChapterId()); // 此处 chapterId 已被正确填充
            entity.setCreator(currentUserId); // 设置创建人
            entity.setOperator(currentUserId); // 设置操作人
            return entity;
        }).collect(Collectors.toList());

        if (!entitiesToInsert.isEmpty()) {
            this.saveBatch(entitiesToInsert);
        }

        // 6. 返回新生成的ID列表
        return entitiesToInsert.stream()
                .map(ApplicationMaterialsTextbookMapping::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ApplicationMaterialsTextbookMappingReturnParam> getPage(ApplicationMaterialsTextbookMappingPageSearchParam param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getTextbookId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }
        Page<ApplicationMaterialsTextbookMappingReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        return baseMapper.getPage(param, page);
    }
}

