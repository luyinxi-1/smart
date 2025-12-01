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
    public Long insertMapping(Long textbookId, Long applicationMaterialId, String textbookCatalogName, Long textbookCatalogId, String textbookCatalogUuId) {
        // 1. 基础参数校验
        if (ObjectUtils.isEmpty(textbookId) || ObjectUtils.isEmpty(applicationMaterialId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID和应用素材ID不能为空");
        }

        // 2. 解析章节ID
        Long finalTextbookCatalogId = textbookCatalogId; // 优先使用传入的 textbookCatalogId

        // 如果 textbookCatalogId 未提供，但 textbookCatalogUuId 提供了，则进行查询转换
        if (finalTextbookCatalogId == null && textbookCatalogUuId != null && !textbookCatalogUuId.trim().isEmpty()) {
            LambdaQueryWrapper<TextbookCatalog> catalogQuery = new LambdaQueryWrapper<TextbookCatalog>()
                    .eq(TextbookCatalog::getCatalogUuid, textbookCatalogUuId)
                    .select(TextbookCatalog::getId); // 仅查询ID字段以提高性能

            TextbookCatalog textbookCatalog = textbookCatalogMapper.selectOne(catalogQuery);

            if (textbookCatalog == null) {
                // 如果根据UUID找不到章节，则抛出异常
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "无效的章节UUID: " + textbookCatalogUuId);
            }
            finalTextbookCatalogId = textbookCatalog.getId(); // 将查询到的ID赋值给 finalTextbookCatalogId
        }

        // 3. 最终校验：确保章节ID、章节名称已确定
        if (finalTextbookCatalogId == null || ObjectUtils.isEmpty(textbookCatalogName)) {
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
                    existingMapping.getTextbookCatalogName(),
                    existingMapping.getTextbookCatalogId()
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
        mapping.setTextbookCatalogName(textbookCatalogName);
        mapping.setTextbookCatalogId(finalTextbookCatalogId); // 使用最终确定的章节ID
        mapping.setTextbookCatalogId2(finalTextbookCatalogId); // 设置备用章节ID，与主章节ID相同
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

        // 1.5 如果传入了 textbookCatalogUuId，则通过它查询并填充 textbookCatalogId 和 textbookCatalogName
        // 筛选出所有需要通过 UUID 解析 textbookCatalogId 的 DTO
        List<String> uuidsToResolve = mappings.stream()
                .filter(dto -> dto.getTextbookCatalogId() == null && dto.getTextbookCatalogUuId() != null && !dto.getTextbookCatalogUuId().trim().isEmpty())
                .map(ApplicationMaterialsTextbookMappingDto::getTextbookCatalogUuId)
                .distinct()
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(uuidsToResolve)) {
            // 批量查询对应的章节目录信息（必须属于该教材）
            List<TextbookCatalog> catalogs = textbookCatalogMapper.selectList(
                    new LambdaQueryWrapper<TextbookCatalog>()
                            .eq(TextbookCatalog::getTextbookId, textbookId)  // 确保章节属于该教材
                            .in(TextbookCatalog::getCatalogUuid, uuidsToResolve)
                            .select(TextbookCatalog::getId, TextbookCatalog::getCatalogUuid, TextbookCatalog::getCatalogName)
            );
            // 创建 UUID -> Catalog对象的映射，方便快速查找ID和名称
            Map<String, TextbookCatalog> uuidToCatalogMap = catalogs.stream()
                    .collect(Collectors.toMap(TextbookCatalog::getCatalogUuid, catalog -> catalog));
            // 检查是否有UUID未找到对应的目录
            if (uuidToCatalogMap.size() != uuidsToResolve.size()) {
                String notFoundUuids = uuidsToResolve.stream()
                        .filter(uuid -> !uuidToCatalogMap.containsKey(uuid))
                        .collect(Collectors.joining(", "));
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "以下章节UUID无效或不存在或不属于该教材: " + notFoundUuids);
            }
            // 回填 textbookCatalogId 和 textbookCatalogName 到DTO中
            for (ApplicationMaterialsTextbookMappingDto dto : mappings) {
                if (dto.getTextbookCatalogId() == null && dto.getTextbookCatalogUuId() != null) {
                    TextbookCatalog catalog = uuidToCatalogMap.get(dto.getTextbookCatalogUuId());
                    if (catalog == null) {
                        // 兜底校验，理论上不会进入此分支
                        throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                                "未能找到章节UUID: " + dto.getTextbookCatalogUuId() + " 对应的章节信息");
                    }
                    dto.setTextbookCatalogId(catalog.getId());
                    dto.setTextbookCatalogName(catalog.getCatalogName());
                }
                
                // 如果textbookCatalogId2为空但textbookCatalogId不为空，则将textbookCatalogId2设置为与textbookCatalogId相同的值
                if (dto.getTextbookCatalogId2() == null && dto.getTextbookCatalogId() != null) {
                    dto.setTextbookCatalogId2(dto.getTextbookCatalogId());
                }
            }
        }

        // 2. 【前置校验】
        // 2.1 检查请求列表内部是否存在重复的应用素材ID（跳过null值）
        Set<Long> uniqueApplicationMaterialIdsInRequest = new HashSet<>();
        for (ApplicationMaterialsTextbookMappingDto dto : mappings) {
            Long applicationMaterialId = dto.getApplicationMaterialId();
            // 允许应用素材ID为空，但如果不为空则检查重复
            if (applicationMaterialId != null) {
                if (!uniqueApplicationMaterialIdsInRequest.add(applicationMaterialId)) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                            "请求参数中存在重复的应用素材ID: " + applicationMaterialId);
                }
            }
        }

        // 2.2 校验教材ID是否存在
        if (textbookMapper.selectById(textbookId) == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不存在，请检查后重试！");
        }

        // 2.3 批量校验所有涉及的应用素材ID是否存在（排除null值）
        Set<Long> applicationMaterialIds = mappings.stream()
                .map(ApplicationMaterialsTextbookMappingDto::getApplicationMaterialId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (!applicationMaterialIds.isEmpty()) {
            long existingApplicationMaterialCount = applicationMaterialsMapper.selectCount(
                    new LambdaQueryWrapper<ApplicationMaterials>().in(ApplicationMaterials::getId, applicationMaterialIds));
            if (existingApplicationMaterialCount != applicationMaterialIds.size()) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "部分应用素材ID不存在，请检查后重试！");
            }
        }

        // 2.4 校验章节ID是否存在且属于该教材，并回填章节名称
        Set<Long> chapterIdsToValidate = mappings.stream()
                .map(ApplicationMaterialsTextbookMappingDto::getTextbookCatalogId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        if (!chapterIdsToValidate.isEmpty()) {
            List<TextbookCatalog> foundCatalogs = textbookCatalogMapper.selectList(
                    new LambdaQueryWrapper<TextbookCatalog>()
                            .eq(TextbookCatalog::getTextbookId, textbookId)  // 校验章节属于该教材
                            .select(TextbookCatalog::getId, TextbookCatalog::getCatalogName)
                            .in(TextbookCatalog::getId, chapterIdsToValidate));
            if (foundCatalogs.size() < chapterIdsToValidate.size()) {
                Set<Long> foundIds = foundCatalogs.stream().map(TextbookCatalog::getId).collect(Collectors.toSet());
                Set<Long> nonExistentIds = new HashSet<>(chapterIdsToValidate);
                nonExistentIds.removeAll(foundIds);
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "新增失败，以下指定的章节ID不存在或不属于该教材: " + nonExistentIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
            
            // 回填章节名称到DTO中（如果DTO中章节名称为空）
            Map<Long, String> catalogIdToNameMap = foundCatalogs.stream()
                    .collect(Collectors.toMap(TextbookCatalog::getId, TextbookCatalog::getCatalogName));
            for (ApplicationMaterialsTextbookMappingDto dto : mappings) {
                if (dto.getTextbookCatalogId() != null && 
                    (dto.getTextbookCatalogName() == null || dto.getTextbookCatalogName().trim().isEmpty())) {
                    String catalogName = catalogIdToNameMap.get(dto.getTextbookCatalogId());
                    if (catalogName != null) {
                        dto.setTextbookCatalogName(catalogName);
                    }
                }
                
                // 如果textbookCatalogId2为空但textbookCatalogId不为空，则将textbookCatalogId2设置为与textbookCatalogId相同的值
                if (dto.getTextbookCatalogId2() == null && dto.getTextbookCatalogId() != null) {
                    dto.setTextbookCatalogId2(dto.getTextbookCatalogId());
                }
            }
        }

        // 3. 【执行删除】删除该教材下所有旧的绑定关系
        this.remove(new LambdaQueryWrapper<ApplicationMaterialsTextbookMapping>()
                .eq(ApplicationMaterialsTextbookMapping::getTextbookId, textbookId));

        // 4. 【最终唯一性校验】
        // 检查 application_material_id（仅当有非空的应用素材ID时才校验）
        if (!uniqueApplicationMaterialIdsInRequest.isEmpty()) {
            LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> conflictCheckWrapper = new LambdaQueryWrapper<>();
            conflictCheckWrapper.in(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, uniqueApplicationMaterialIdsInRequest);

            List<ApplicationMaterialsTextbookMapping> conflictingBindings = this.list(conflictCheckWrapper);
            if (!conflictingBindings.isEmpty()) {
                String existingDetails = conflictingBindings.stream()
                        .map(e -> String.format("应用素材ID:%d，已被教材ID:%d，章节'%s'，章节ID：%d绑定", 
                                e.getApplicationMaterialId(), e.getTextbookId(), e.getTextbookCatalogName(), e.getTextbookCatalogId()))
                        .collect(Collectors.joining("; "));

                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "操作失败，部分应用素材已绑定到本书的其他章节: " + existingDetails);
            }
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
            entity.setTextbookCatalogName(dto.getTextbookCatalogName());
            entity.setTextbookCatalogId(dto.getTextbookCatalogId()); // 此处 textbookCatalogId 已被正确填充
            entity.setTextbookCatalogId2(dto.getTextbookCatalogId2()); // 设置备用章节ID
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

    @Override
    public Long getTextbookIdByChapterId(Long chapterId) {
        if (chapterId == null) {
            return null;
        }
        
        LambdaQueryWrapper<TextbookCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookCatalog::getId, chapterId)
                .select(TextbookCatalog::getTextbookId)
                .last("LIMIT 1");
        
        TextbookCatalog result = textbookCatalogMapper.selectOne(queryWrapper);
        return result != null ? result.getTextbookId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeApplicationMaterialsBindingsByChapterIds(Long textbookId, List<Long> chapterIds) {
        if (textbookId == null || CollectionUtils.isEmpty(chapterIds)) {
            return;
        }
        
        // 删除该教材下指定章节的绑定关系
        LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper
                .eq(ApplicationMaterialsTextbookMapping::getTextbookId, textbookId)
                .in(ApplicationMaterialsTextbookMapping::getTextbookCatalogId, chapterIds);
        
        // 执行批量删除操作
        this.remove(deleteWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> updateApplicationMaterialsBatchByChapters(Long textbookId, List<Long> chapterIds, List<ApplicationMaterialsTextbookMappingDto> mappings) {
        // 1. 基本参数校验
        if (CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyList();
        }

        if (textbookId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        // 1.5 如果传入了 textbookCatalogUuId，则通过它查询并填充 textbookCatalogId 和 textbookCatalogName
        // 筛选出所有需要通过 UUID 解析 textbookCatalogId 的 DTO
        List<String> uuidsToResolve = mappings.stream()
                .filter(dto -> dto.getTextbookCatalogId() == null && dto.getTextbookCatalogUuId() != null && !dto.getTextbookCatalogUuId().trim().isEmpty())
                .map(ApplicationMaterialsTextbookMappingDto::getTextbookCatalogUuId)
                .distinct()
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(uuidsToResolve)) {
            // 批量查询对应的章节目录信息（必须属于该教材）
            List<TextbookCatalog> catalogs = textbookCatalogMapper.selectList(
                    new LambdaQueryWrapper<TextbookCatalog>()
                            .eq(TextbookCatalog::getTextbookId, textbookId)  // 确保章节属于该教材
                            .in(TextbookCatalog::getCatalogUuid, uuidsToResolve)
                            .select(TextbookCatalog::getId, TextbookCatalog::getCatalogUuid, TextbookCatalog::getCatalogName)
            );
            // 创建 UUID -> Catalog对象的映射，方便快速查找ID和名称
            Map<String, TextbookCatalog> uuidToCatalogMap = catalogs.stream()
                    .collect(Collectors.toMap(TextbookCatalog::getCatalogUuid, catalog -> catalog));
            // 检查是否有UUID未找到对应的目录
            if (uuidToCatalogMap.size() != uuidsToResolve.size()) {
                String notFoundUuids = uuidsToResolve.stream()
                        .filter(uuid -> !uuidToCatalogMap.containsKey(uuid))
                        .collect(Collectors.joining(", "));
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "以下章节UUID无效或不存在或不属于该教材: " + notFoundUuids);
            }
            // 回填 textbookCatalogId 和 textbookCatalogName 到DTO中
            for (ApplicationMaterialsTextbookMappingDto dto : mappings) {
                if (dto.getTextbookCatalogId() == null && dto.getTextbookCatalogUuId() != null) {
                    TextbookCatalog catalog = uuidToCatalogMap.get(dto.getTextbookCatalogUuId());
                    if (catalog == null) {
                        // 兜底校验，理论上不会进入此分支
                        throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                                "未能找到章节UUID: " + dto.getTextbookCatalogUuId() + " 对应的章节信息");
                    }
                    dto.setTextbookCatalogId(catalog.getId());
                    dto.setTextbookCatalogName(catalog.getCatalogName());
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
        Set<Long> applicationMaterialIds = mappings.stream()
                .map(ApplicationMaterialsTextbookMappingDto::getApplicationMaterialId)
                .collect(Collectors.toSet());
        if (!applicationMaterialIds.isEmpty()) {
            long existingApplicationMaterialCount = applicationMaterialsMapper.selectCount(
                    new LambdaQueryWrapper<ApplicationMaterials>().in(ApplicationMaterials::getId, applicationMaterialIds));
            if (existingApplicationMaterialCount != applicationMaterialIds.size()) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "部分应用素材ID不存在，请检查后重试！");
            }
        }

        // 2.4 校验章节ID是否存在且属于该教材，并回填章节名称
        // 收集所有需要校验的章节ID（包括catalogList和绑定关系中的章节ID）
        Set<Long> allChapterIdsToValidate = new HashSet<>();
        if (!CollectionUtils.isEmpty(chapterIds)) {
            allChapterIdsToValidate.addAll(chapterIds);
        }
        // 从绑定关系中收集章节ID
        for (ApplicationMaterialsTextbookMappingDto dto : mappings) {
            if (dto.getTextbookCatalogId() != null) {
                allChapterIdsToValidate.add(dto.getTextbookCatalogId());
            }
        }
        
        if (!allChapterIdsToValidate.isEmpty()) {
            List<TextbookCatalog> foundCatalogs = textbookCatalogMapper.selectList(
                    new LambdaQueryWrapper<TextbookCatalog>()
                            .eq(TextbookCatalog::getTextbookId, textbookId)  // 校验章节属于该教材
                            .select(TextbookCatalog::getId, TextbookCatalog::getCatalogName)
                            .in(TextbookCatalog::getId, allChapterIdsToValidate));
            if (foundCatalogs.size() < allChapterIdsToValidate.size()) {
                Set<Long> foundIds = foundCatalogs.stream().map(TextbookCatalog::getId).collect(Collectors.toSet());
                Set<Long> nonExistentIds = new HashSet<>(allChapterIdsToValidate);
                nonExistentIds.removeAll(foundIds);
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "更新失败，以下指定的章节ID不存在或不属于该教材: " + nonExistentIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
            
            // 回填章节名称到DTO中（如果DTO中章节名称为空）
            Map<Long, String> catalogIdToNameMap = foundCatalogs.stream()
                    .collect(Collectors.toMap(TextbookCatalog::getId, TextbookCatalog::getCatalogName));
            for (ApplicationMaterialsTextbookMappingDto dto : mappings) {
                if (dto.getTextbookCatalogId() != null && 
                    (dto.getTextbookCatalogName() == null || dto.getTextbookCatalogName().trim().isEmpty())) {
                    String catalogName = catalogIdToNameMap.get(dto.getTextbookCatalogId());
                    if (catalogName != null) {
                        dto.setTextbookCatalogName(catalogName);
                    }
                }
            }
        }

        // 3. 【执行删除】删除该教材下指定章节的旧绑定关系（如果提供了章节ID列表）
        if (!CollectionUtils.isEmpty(chapterIds)) {
            LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper
                    .eq(ApplicationMaterialsTextbookMapping::getTextbookId, textbookId)
                    .in(ApplicationMaterialsTextbookMapping::getTextbookCatalogId, chapterIds);
            this.remove(deleteWrapper);
        }

        // 4. 【最终唯一性校验】
        // 检查 application_material_id 是否已被其他教材或章节绑定
//        LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> conflictCheckWrapper = new LambdaQueryWrapper<>();
//        conflictCheckWrapper.in(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, uniqueApplicationMaterialIdsInRequest);
//
//        List<ApplicationMaterialsTextbookMapping> conflictingBindings = this.list(conflictCheckWrapper);
//        if (!conflictingBindings.isEmpty()) {
//            String existingDetails = conflictingBindings.stream()
//                    .map(e -> String.format("应用素材ID:%d，已被教材ID:%d，章节'%s'，章节ID：%d绑定",
//                            e.getApplicationMaterialId(), e.getTextbookId(), e.getTextbookCatalogName(), e.getTextbookCatalogId()))
//                    .collect(Collectors.joining("; "));
//
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "操作失败，部分应用素材已绑定到其他教材或章节: " + existingDetails);
//        }

        // 5. 【执行新增】

        if (!uniqueApplicationMaterialIdsInRequest.isEmpty()) {
            // 构建删除条件：删除指定教材下，指定applicationMaterialId的记录
            LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.in(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, uniqueApplicationMaterialIdsInRequest);

            this.remove(deleteWrapper);
        }

        // 获取当前登录用户
        Long currentUserId = com.upc.common.utils.UserUtils.get() != null ? 
            com.upc.common.utils.UserUtils.get().getId() : null;
        
        // 数据转换与批量插入
        List<ApplicationMaterialsTextbookMapping> entitiesToInsert = mappings.stream().map(dto -> {
            ApplicationMaterialsTextbookMapping entity = new ApplicationMaterialsTextbookMapping();
            entity.setTextbookId(textbookId);
            entity.setApplicationMaterialId(dto.getApplicationMaterialId());
            entity.setTextbookCatalogName(dto.getTextbookCatalogName());
            entity.setTextbookCatalogId(dto.getTextbookCatalogId());
            entity.setTextbookCatalogId2(dto.getTextbookCatalogId2());
            
            // 如果textbookCatalogId2为空但textbookCatalogId不为空，则将textbookCatalogId2设置为与textbookCatalogId相同的值
/*            if (dto.getTextbookCatalogId2() == null && dto.getTextbookCatalogId() != null) {
                entity.setTextbookCatalogId2(dto.getTextbookCatalogId());
            } else {
                entity.setTextbookCatalogId2(dto.getTextbookCatalogId2());
            }*/
            
            entity.setCreator(currentUserId);
            entity.setOperator(currentUserId);
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
}

