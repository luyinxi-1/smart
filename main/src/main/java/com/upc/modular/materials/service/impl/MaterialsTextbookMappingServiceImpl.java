package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingDto;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.MaterialsTextbookMappingMapper;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.IMaterialsTextbookMappingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.impl.TeacherServiceImpl;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.upc.modular.textbook.entity.TextbookCatalog;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.upc.utils.CreatePage.createPage;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mjh
 * @since 2025-08-23
 */
@Service
public class MaterialsTextbookMappingServiceImpl extends ServiceImpl<MaterialsTextbookMappingMapper, MaterialsTextbookMapping> implements IMaterialsTextbookMappingService {


//    private TeachingMaterialsServiceImpl teachingMaterialsService;

    @Autowired
    private TeachingMaterialsMapper teachingMaterialsMapper;

    @Autowired
    private TeacherServiceImpl teacherService;

    @Autowired
    private MaterialsTextbookMappingMapper baseMapper;
    @Autowired
    private TextbookMapper textbookMapper;
    @Resource
    private TextbookCatalogMapper textbookCatalogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertMapping(Long textbookId, Long materialId, String chapterName, Long chapterId, String chapterUuid) {
        // 1. 基础参数校验
        if (ObjectUtils.isEmpty(textbookId) || ObjectUtils.isEmpty(materialId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID和素材ID不能为空");
        }

        // 2. 【新增逻辑】解析章节ID
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

        // 4. 唯一性校验：检查该素材是否已被绑定 (逻辑保持不变)
        LambdaQueryWrapper<MaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaterialsTextbookMapping::getMaterialId, materialId);
        MaterialsTextbookMapping existingMapping = this.getOne(queryWrapper);
        if (existingMapping != null) {
            String errorMessage = String.format(
                    "该素材 (ID: %d) 已被绑定到教材 (ID: %d) 的章节 '%s', ID为 '%d'。一个素材只能绑定一次。",
                    materialId,
                    existingMapping.getTextbookId(),
                    existingMapping.getChapterName(),
                    existingMapping.getChapterId()
            );
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, errorMessage);
        }

        // 5. 如果校验通过，则创建并保存新的绑定关系
        MaterialsTextbookMapping mapping = new MaterialsTextbookMapping();
        mapping.setTextbookId(textbookId);
        mapping.setMaterialId(materialId);
        mapping.setChapterName(chapterName);
        mapping.setChapterId(finalChapterId); // 使用最终确定的章节ID

        if (this.save(mapping)) {
            return mapping.getId();
        }
        return null;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> insertMappingBatch(List<MaterialsTextbookMappingDto> mappings) {
        // 1. 基本参数校验
        if (CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyList();
        }

        // 获取教材ID（从第一个元素中获取，假定所有元素的教材ID相同）
        Long textbookId = mappings.get(0).getTextbookId();
        if (textbookId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        // 1.5 如果传入了 chapterUuid，则通过它查询并填充 chapterId
        // 筛选出所有需要通过 UUID 解析 chapterId 的 DTO
        List<String> uuidsToResolve = mappings.stream()
                .filter(dto -> dto.getChapterId() == null && dto.getChapterUuid() != null && !dto.getChapterUuid().trim().isEmpty())
                .map(MaterialsTextbookMappingDto::getChapterUuid)
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
            for (MaterialsTextbookMappingDto dto : mappings) {
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
        // 2.1 检查请求列表内部是否存在重复的素材ID
        Set<Long> uniqueMaterialIdsInRequest = new HashSet<>();
        for (MaterialsTextbookMappingDto dto : mappings) {
            if (dto.getMaterialId() == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "请求参数中存在空的素材ID");
            }
            if (!uniqueMaterialIdsInRequest.add(dto.getMaterialId())) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "请求参数中存在重复的素材ID: " + dto.getMaterialId());
            }
        }

        // 2.2 【完整校验】批量校验所有涉及的教材ID是否存在
        Set<Long> textbookIds = mappings.stream().map(MaterialsTextbookMappingDto::getTextbookId).collect(Collectors.toSet());
        if (!textbookIds.isEmpty()) {
            long existingTextbookCount = textbookMapper.selectCount(new LambdaQueryWrapper<Textbook>().in(Textbook::getId, textbookIds));
            if (existingTextbookCount != textbookIds.size()) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "部分教材ID不存在，请检查后重试！");
            }
        }
        // 2.3 【完整校验】批量校验所有涉及的素材ID是否存在
        Set<Long> materialIds = mappings.stream().map(MaterialsTextbookMappingDto::getMaterialId).collect(Collectors.toSet());
        if (!materialIds.isEmpty()) {
            long existingMaterialCount = teachingMaterialsMapper.selectCount(new LambdaQueryWrapper<TeachingMaterials>().in(TeachingMaterials::getId, materialIds));
            if (existingMaterialCount != materialIds.size()) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "部分素材ID不存在，请检查后重试！");
            }
        }

        // 3. 【执行删除】
        // 删除该教材下所有旧的绑定关系
        this.remove(new LambdaQueryWrapper<MaterialsTextbookMapping>()
                .eq(MaterialsTextbookMapping::getTextbookId, textbookId));
                
        // 4. 【最终唯一性校验】
        // 检查 material_id
        LambdaQueryWrapper<MaterialsTextbookMapping> conflictCheckWrapper = new LambdaQueryWrapper<>();
        conflictCheckWrapper.in(MaterialsTextbookMapping::getMaterialId, uniqueMaterialIdsInRequest);

        List<MaterialsTextbookMapping> conflictingBindings = this.list(conflictCheckWrapper);
        if (!conflictingBindings.isEmpty()) {
            String existingDetails = conflictingBindings.stream()
                    .map(e -> String.format("素材ID:%d，已被教材ID:%d，章节:'%s'，章节ID：'%d'绑定", e.getMaterialId(), e.getTextbookId(), e.getChapterName(), e.getChapterId()))
                    .collect(Collectors.joining("; "));

            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "操作失败，部分素材已绑定到本书的其他章节: " + existingDetails);
        }
        // 5. 【执行新增】
        // 数据转换与批量插入
        List<MaterialsTextbookMapping> entitiesToInsert = mappings.stream().map(dto -> {
            MaterialsTextbookMapping entity = new MaterialsTextbookMapping();
            entity.setTextbookId(dto.getTextbookId());
            entity.setMaterialId(dto.getMaterialId());
            entity.setChapterName(dto.getChapterName());
            entity.setChapterId(dto.getChapterId()); // 此处 chapterId 已被正确填充
            return entity;
        }).collect(Collectors.toList());

        if (!entitiesToInsert.isEmpty()) {
            this.saveBatch(entitiesToInsert);
        }

        // 6. 返回新生成的ID列表
        return entitiesToInsert.stream()
                .map(MaterialsTextbookMapping::getId)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> insertMappingBatchByChapters(Long textbookId, List<Long> chapterIds, List<MaterialsTextbookMappingDto> mappings) {
        // 1. 基本参数校验
        if (CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyList();
        }

        if (textbookId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }
        
        if (CollectionUtils.isEmpty(chapterIds)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "章节ID列表不能为空");
        }

        // 1.5 如果传入了 chapterUuid，则通过它查询并填充 chapterId
        // 筛选出所有需要通过 UUID 解析 chapterId 的 DTO
        List<String> uuidsToResolve = mappings.stream()
                .filter(dto -> dto.getChapterId() == null && dto.getChapterUuid() != null && !dto.getChapterUuid().trim().isEmpty())
                .map(MaterialsTextbookMappingDto::getChapterUuid)
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
            for (MaterialsTextbookMappingDto dto : mappings) {
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
        // 2.1 检查请求列表内部是否存在重复的素材ID
        Set<Long> uniqueMaterialIdsInRequest = new HashSet<>();
        for (MaterialsTextbookMappingDto dto : mappings) {
            if (dto.getMaterialId() == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "请求参数中存在空的素材ID");
            }
            if (!uniqueMaterialIdsInRequest.add(dto.getMaterialId())) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "请求参数中存在重复的素材ID: " + dto.getMaterialId());
            }
        }

        // 2.2 【完整校验】批量校验所有涉及的教材ID是否存在
        Set<Long> textbookIds = mappings.stream().map(MaterialsTextbookMappingDto::getTextbookId).collect(Collectors.toSet());
        // 添加当前教材ID确保在校验范围内
        textbookIds.add(textbookId);
        if (!textbookIds.isEmpty()) {
            long existingTextbookCount = textbookMapper.selectCount(new LambdaQueryWrapper<Textbook>().in(Textbook::getId, textbookIds));
            if (existingTextbookCount != textbookIds.size()) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "部分教材ID不存在，请检查后重试！");
            }
        }
        // 2.3 【完整校验】批量校验所有涉及的素材ID是否存在
        Set<Long> materialIds = mappings.stream().map(MaterialsTextbookMappingDto::getMaterialId).collect(Collectors.toSet());
        if (!materialIds.isEmpty()) {
            long existingMaterialCount = teachingMaterialsMapper.selectCount(new LambdaQueryWrapper<TeachingMaterials>().in(TeachingMaterials::getId, materialIds));
            if (existingMaterialCount != materialIds.size()) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "部分素材ID不存在，请检查后重试！");
            }
        }

        // 3. 【执行删除】
        // 删除该教材下指定章节的旧绑定关系
        this.remove(new LambdaQueryWrapper<MaterialsTextbookMapping>()
                .eq(MaterialsTextbookMapping::getTextbookId, textbookId)
                .in(MaterialsTextbookMapping::getChapterId, chapterIds));

        // 4. 【最终唯一性校验】
        // 检查 material_id
        LambdaQueryWrapper<MaterialsTextbookMapping> conflictCheckWrapper = new LambdaQueryWrapper<>();
        conflictCheckWrapper.in(MaterialsTextbookMapping::getMaterialId, uniqueMaterialIdsInRequest);

        List<MaterialsTextbookMapping> conflictingBindings = this.list(conflictCheckWrapper);
        if (!conflictingBindings.isEmpty()) {
            String existingDetails = conflictingBindings.stream()
                    .map(e -> String.format("素材ID:%d，已被教材ID:%d，章节'%s'，章节ID：%d绑定", e.getMaterialId(), e.getTextbookId(), e.getChapterName(), e.getChapterId()))
                    .collect(Collectors.joining("; "));

            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "操作失败，部分素材已绑定到本书的其他章节: " + existingDetails);
        }
        // 5. 【执行新增】
        // 数据转换与批量插入
        List<MaterialsTextbookMapping> entitiesToInsert = mappings.stream().map(dto -> {
            MaterialsTextbookMapping entity = new MaterialsTextbookMapping();
            entity.setTextbookId(dto.getTextbookId());
            entity.setMaterialId(dto.getMaterialId());
            entity.setChapterName(dto.getChapterName());
            entity.setChapterId(dto.getChapterId()); // 此处 chapterId 已被正确填充
            return entity;
        }).collect(Collectors.toList());

        if (!entitiesToInsert.isEmpty()) {
            this.saveBatch(entitiesToInsert);
        }

        // 6. 返回新生成的ID列表
        return entitiesToInsert.stream()
                .map(MaterialsTextbookMapping::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MaterialsTextbookMappingReturnParam> getPage(MaterialsTextbookMappingPageSearchParam param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getTextbookId()))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        Page<MaterialsTextbookMappingReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        return baseMapper.getPage(param, page);
    }

    @Override
    public TeachingMaterialsReturnVo getMaterialsByMappingId(Long id) {
        if (id == null)
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，参数 id 不能为空");

        MaterialsTextbookMapping mapping = this.getById(id);
        if (ObjectUtils.isEmpty(mapping))
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "指定的教材素材关系不存在");

        Long materialId = mapping.getMaterialId();
        TeachingMaterials materials = teachingMaterialsMapper.selectById(materialId);
        if (ObjectUtils.isEmpty(materials))
            throw new BusinessException(BusinessErrorEnum.FILE_NOT_EXIST, "对应的素材不存在");

        Long currentUserId = UserUtils.get().getId();
        Integer userType = UserUtils.get().getUserType();

        // 权限检查逻辑
        if (!materials.getIsPublic() && !materials.getCreator().equals(currentUserId) && userType != 0) {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS, "您没有权限查看此文件");
        }

        TeachingMaterialsReturnVo materialsReturnVo = new TeachingMaterialsReturnVo();
        BeanUtils.copyProperties(materials, materialsReturnVo);


        Teacher teacher = teacherService.getById(materials.getCreator());
        if (teacher != null && ObjectUtils.isNotEmpty(teacher.getName())) {
            materialsReturnVo.setAuthorName(teacher.getName());
        }


        if (materials.getCreator() != null) {
            materialsReturnVo.setIsCreator(materials.getCreator().equals(currentUserId));
        } else {
            materialsReturnVo.setIsCreator(false);
        }

        return materialsReturnVo;
    }
    
    @Override
    public void removeMappingsByChapterIds(Long textbookId, List<Long> chapterIds) {
        if (textbookId == null || CollectionUtils.isEmpty(chapterIds)) {
            return;
        }
        
        this.remove(new LambdaQueryWrapper<MaterialsTextbookMapping>()
                .eq(MaterialsTextbookMapping::getTextbookId, textbookId)
                .in(MaterialsTextbookMapping::getChapterId, chapterIds));
    }
    
    @Override
    public Long getTextbookIdByChapterId(Long chapterId) {
        if (chapterId == null) {
            return null;
        }
        
        LambdaQueryWrapper<MaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaterialsTextbookMapping::getChapterId, chapterId)
                .select(MaterialsTextbookMapping::getTextbookId)
                .last("LIMIT 1");
        
        MaterialsTextbookMapping mapping = this.getOne(queryWrapper);
        return mapping != null ? mapping.getTextbookId() : null;
    }
}
