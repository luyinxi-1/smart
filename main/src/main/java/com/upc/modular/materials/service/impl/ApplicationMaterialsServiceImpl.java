package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsPageParam;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsSaveParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsDetailVO;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsVO;
import com.upc.modular.materials.entity.ApplicationMaterials;
import com.upc.modular.materials.entity.ApplicationMaterialsMapping;
import com.upc.modular.materials.entity.ApplicationMaterialsTextbookMapping;
import com.upc.modular.materials.mapper.ApplicationMaterialsMappingMapper;
import com.upc.modular.materials.mapper.ApplicationMaterialsMapper;
import com.upc.modular.materials.mapper.ApplicationMaterialsTextbookMappingMapper;
import com.upc.modular.materials.service.IApplicationMaterialsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 应用素材服务实现类
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Slf4j
@Service
public class ApplicationMaterialsServiceImpl extends ServiceImpl<ApplicationMaterialsMapper, ApplicationMaterials> implements IApplicationMaterialsService {

    @Autowired
    private ApplicationMaterialsMappingMapper applicationMaterialsMappingMapper;

    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;

    @Autowired
    private ApplicationMaterialsTextbookMappingMapper applicationMaterialsTextbookMappingMapper;

    @Autowired
    private ApplicationMaterialsMapper applicationMaterialsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveApplicationMaterials(ApplicationMaterialsSaveParam param) {
        // 参数校验
        if (param == null || param.getName() == null || param.getName().trim().isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "应用素材名称不能为空");
        }
        
        // 创建应用素材实体
        ApplicationMaterials applicationMaterials = new ApplicationMaterials();
        BeanUtils.copyProperties(param, applicationMaterials);
        
        // 设置默认值
        if (applicationMaterials.getStatus() == null) {
            applicationMaterials.setStatus(0); // 默认未发布
        }
        
        // 执行保存
        boolean saveResult = this.save(applicationMaterials);
        if (!saveResult) {
            throw new BusinessException(BusinessErrorEnum.BUSINESS_ERROR, "保存应用素材失败");
        }
        
        // 关联教学素材（如果有）
        if (!CollectionUtils.isEmpty(param.getTeachingMaterialIds())) {
            relateTeachingMaterials(applicationMaterials.getId(), param.getTeachingMaterialIds());
        }
        
        // 同步创建教材绑定关系（如果有教材ID和章节ID）
        syncTextbookMappingOnSave(applicationMaterials.getId(), param);
        
        return applicationMaterials.getId();
    }

    /**
     * 保存应用素材时同步创建教材绑定关系
     * 
     * @param applicationMaterialId 应用素材ID
     * @param param 保存参数
     */
    private void syncTextbookMappingOnSave(Long applicationMaterialId, ApplicationMaterialsSaveParam param) {
        // 如果没有教材ID，不需要创建绑定
        if (param.getTextbookId() == null) {
            return;
        }
        
        // 解析章节ID
        Long chapterId = param.getTextbookCatalogId();
        Long chapterId2 = param.getTextbookCatalogId2();
        String chapterName = "";
        
        if (chapterId == null && param.getTextbookCatalogUuId() != null && !param.getTextbookCatalogUuId().trim().isEmpty()) {
            // 使用UUID查询章节，必须属于该教材
            LambdaQueryWrapper<TextbookCatalog> catalogQuery = new LambdaQueryWrapper<TextbookCatalog>()
                    .eq(TextbookCatalog::getTextbookId, param.getTextbookId())  // 校验章节属于该教材
                    .eq(TextbookCatalog::getCatalogUuid, param.getTextbookCatalogUuId())
                    .select(TextbookCatalog::getId, TextbookCatalog::getCatalogName);
            
            TextbookCatalog textbookCatalog = textbookCatalogMapper.selectOne(catalogQuery);
            
            if (textbookCatalog == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, 
                        "提供的章节UUID无效或不属于该教材: " + param.getTextbookCatalogUuId());
            }
            chapterId = textbookCatalog.getId();
            //chapterId2 = chapterId2 != null ? chapterId2 : chapterId; // 如果未提供chapterId2，则与chapterId相同
            chapterName = textbookCatalog.getCatalogName();
        } else if (chapterId != null) {
            // 如果直接提供了章节ID，查询章节并验证是否属于该教材
            LambdaQueryWrapper<TextbookCatalog> catalogQuery = new LambdaQueryWrapper<TextbookCatalog>()
                    .eq(TextbookCatalog::getId, chapterId)
                    .eq(TextbookCatalog::getTextbookId, param.getTextbookId())  // 校验章节属于该教材
                    .select(TextbookCatalog::getCatalogName);
            
            TextbookCatalog catalog = textbookCatalogMapper.selectOne(catalogQuery);
            if (catalog == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, 
                        "章节ID不存在或不属于该教材: " + chapterId);
            }
            chapterName = catalog.getCatalogName();
            //chapterId2 = chapterId2 != null ? chapterId2 : chapterId; // 如果未提供chapterId2，则与chapterId相同
        } else {
            // 如果没有提供chapterId，则chapterId2保持原值
            chapterId2 = param.getTextbookCatalogId2();
        }
        
        // 检查是否已存在绑定关系
        LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, applicationMaterialId);
        ApplicationMaterialsTextbookMapping existingMapping = applicationMaterialsTextbookMappingMapper.selectOne(queryWrapper);
        
        if (existingMapping == null) {
            // 获取当前登录用户
            Long currentUserId = UserUtils.get() != null ? UserUtils.get().getId() : null;
            
            // 创建新的绑定关系（允许章节ID为null）
            ApplicationMaterialsTextbookMapping mapping = new ApplicationMaterialsTextbookMapping();
            mapping.setApplicationMaterialId(applicationMaterialId);
            mapping.setTextbookId(param.getTextbookId());
            mapping.setTextbookCatalogId(chapterId);  // 可以为null
            mapping.setTextbookCatalogId2(chapterId2);  // 设置备用章节ID
            mapping.setTextbookCatalogName(chapterName);  // 如果章节ID为null，则为空字符串
            mapping.setCreator(currentUserId);
            mapping.setOperator(currentUserId);
            applicationMaterialsTextbookMappingMapper.insert(mapping);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateApplicationMaterials(ApplicationMaterialsSaveParam param) {
        // 参数校验
        if (param == null || param.getId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "应用素材ID不能为空");
        }
        
        // 检查应用素材是否存在
        ApplicationMaterials existingMaterial = this.getById(param.getId());
        if (existingMaterial == null) {
            throw new BusinessException(BusinessErrorEnum.NO_EXIT, "应用素材不存在");
        }
        
        // 【新增逻辑】解析章节ID：如果textbookCatalogId为空但textbookCatalogUuId不为空，则根据UUID查询转换
        Long finalChapterId = param.getTextbookCatalogId();
        Long finalChapterId2 = param.getTextbookCatalogId2();
        if (finalChapterId == null && param.getTextbookCatalogUuId() != null && !param.getTextbookCatalogUuId().trim().isEmpty()) {
            // 如果有教材ID，必须验证章节属于该教材
            LambdaQueryWrapper<TextbookCatalog> catalogQuery = new LambdaQueryWrapper<TextbookCatalog>()
                    .eq(TextbookCatalog::getCatalogUuid, param.getTextbookCatalogUuId());
            
            // 如果提供了教材ID，加上教材ID的过滤条件
            if (param.getTextbookId() != null) {
                catalogQuery.eq(TextbookCatalog::getTextbookId, param.getTextbookId());
            }
            
            catalogQuery.select(TextbookCatalog::getId);
            
            TextbookCatalog textbookCatalog = textbookCatalogMapper.selectOne(catalogQuery);
            
            if (textbookCatalog == null) {
                if (param.getTextbookId() != null) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, 
                            "提供的章节UUID无效或不属于该教材: " + param.getTextbookCatalogUuId());
                } else {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, 
                            "提供的章节UUID无效: " + param.getTextbookCatalogUuId());
                }
            }
            
            finalChapterId = textbookCatalog.getId();
            // 如果未提供textbookCatalogId2，则将其设置为与textbookCatalogId相同
            //finalChapterId2 = finalChapterId2 != null ? finalChapterId2 : finalChapterId;
        } else if (finalChapterId != null && param.getTextbookId() != null) {
            // 如果直接提供了章节ID和教材ID，验证章节是否属于该教材
            LambdaQueryWrapper<TextbookCatalog> catalogQuery = new LambdaQueryWrapper<TextbookCatalog>()
                    .eq(TextbookCatalog::getId, finalChapterId)
                    .eq(TextbookCatalog::getTextbookId, param.getTextbookId())  // 校验章节属于该教材
                    .select(TextbookCatalog::getId);
            
            TextbookCatalog catalog = textbookCatalogMapper.selectOne(catalogQuery);
            if (catalog == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, 
                        "章节ID不存在或不属于该教材: " + finalChapterId);
            }
            
            // 如果未提供textbookCatalogId2，则将其设置为与textbookCatalogId相同
            //finalChapterId2 = finalChapterId2 != null ? finalChapterId2 : finalChapterId;
        }
        
        // 更新应用素材
        ApplicationMaterials applicationMaterials = new ApplicationMaterials();
        BeanUtils.copyProperties(param, applicationMaterials);
        
        boolean updateResult = applicationMaterialsMapper.updateByApplicationMaterialId(applicationMaterials);
        if (!updateResult) {
            throw new BusinessException(BusinessErrorEnum.BUSINESS_ERROR, "更新应用素材失败");
        }
        
        // 更新关联的教学素材（如果有）
        if (param.getTeachingMaterialIds() != null) {
            // 先删除旧的关联
            applicationMaterialsMappingMapper.deleteByApplicationMaterialId(param.getId());
            
            // 添加新的关联
            if (!param.getTeachingMaterialIds().isEmpty()) {
                List<ApplicationMaterialsMapping> mappings = new ArrayList<>();
                for (Long teachingMaterialId : param.getTeachingMaterialIds()) {
                    ApplicationMaterialsMapping mapping = new ApplicationMaterialsMapping();
                    mapping.setApplicationMaterialId(param.getId());
                    mapping.setTeachingMaterialId(teachingMaterialId);
                    mappings.add(mapping);
                }
                applicationMaterialsMappingMapper.batchInsert(mappings);
            }
        }
        
        // 更新教材绑定关系（如果有教材ID）
        updateTextbookMappingOnUpdate(param, finalChapterId, finalChapterId2);
        
        return true;
    }
    
    /**
     * 更新应用素材时同步更新教材绑定关系
     * 
     * @param param 保存参数
     * @param finalChapterId 章节ID
     * @param finalChapterId2 备用章节ID
     */
    private void updateTextbookMappingOnUpdate(ApplicationMaterialsSaveParam param, Long finalChapterId, Long finalChapterId2) {
        if (param.getTextbookId() != null) {
            // 查询现有绑定关系
            LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, param.getId());
            ApplicationMaterialsTextbookMapping existingMapping = applicationMaterialsTextbookMappingMapper.selectOne(queryWrapper);
            
            // 获取当前登录用户作为操作人
            Long currentUserId = UserUtils.get() != null ? UserUtils.get().getId() : null;
            
            if (existingMapping != null) {
                // 更新现有绑定关系
                existingMapping.setTextbookId(param.getTextbookId());
                existingMapping.setTextbookCatalogId(finalChapterId);
                existingMapping.setTextbookCatalogId2(finalChapterId2);
               // existingMapping.setTextbookCatalogId2(finalChapterId2 != null ? finalChapterId2 : finalChapterId); // 如果未提供则与主章节ID一致
                existingMapping.setOperator(currentUserId);
                // 章节名称只在章节ID有效时更新
                if (finalChapterId != null) {
                    LambdaQueryWrapper<TextbookCatalog> catalogQuery = new LambdaQueryWrapper<TextbookCatalog>()
                            .eq(TextbookCatalog::getId, finalChapterId)
                            .select(TextbookCatalog::getCatalogName);
                    TextbookCatalog catalog = textbookCatalogMapper.selectOne(catalogQuery);
                    if (catalog != null) {
                        existingMapping.setTextbookCatalogName(catalog.getCatalogName());
                    }
                }
                applicationMaterialsTextbookMappingMapper.updateById(existingMapping);
            } else {
                // 创建新的绑定关系
                String chapterName = "";
                if (finalChapterId != null) {
                    LambdaQueryWrapper<TextbookCatalog> catalogQuery = new LambdaQueryWrapper<TextbookCatalog>()
                            .eq(TextbookCatalog::getId, finalChapterId)
                            .select(TextbookCatalog::getCatalogName);
                    TextbookCatalog catalog = textbookCatalogMapper.selectOne(catalogQuery);
                    if (catalog != null) {
                        chapterName = catalog.getCatalogName();
                    }
                }
                
                ApplicationMaterialsTextbookMapping mapping = new ApplicationMaterialsTextbookMapping();
                mapping.setApplicationMaterialId(param.getId());
                mapping.setTextbookId(param.getTextbookId());
                mapping.setTextbookCatalogId(finalChapterId);
                mapping.setTextbookCatalogId2(finalChapterId2);
                //mapping.setTextbookCatalogId2(finalChapterId2 != null ? finalChapterId2 : finalChapterId); // 如果未提供则与主章节ID一致
                mapping.setTextbookCatalogName(chapterName);
                mapping.setCreator(currentUserId);
                mapping.setOperator(currentUserId);
                applicationMaterialsTextbookMappingMapper.insert(mapping);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteApplicationMaterials(Long id) {
        if (id == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "应用素材ID不能为空");
        }
        
        // 检查应用素材是否存在
        ApplicationMaterials existingMaterial = this.getById(id);
        if (existingMaterial == null) {
            throw new BusinessException(BusinessErrorEnum.NO_EXIT, "应用素材不存在");
        }
        
        // 删除关联的教学素材
        applicationMaterialsMappingMapper.deleteByApplicationMaterialId(id);
        
        // 删除教材绑定关系
        LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, id);
        applicationMaterialsTextbookMappingMapper.delete(queryWrapper);
        
        // 删除应用素材
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteApplicationMaterials(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "应用素材ID列表不能为空");
        }
        
        // 删除关联的教学素材
        for (Long id : ids) {
            applicationMaterialsMappingMapper.deleteByApplicationMaterialId(id);
        }
        
        // 批量删除教材绑定关系
        LambdaQueryWrapper<ApplicationMaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ApplicationMaterialsTextbookMapping::getApplicationMaterialId, ids);
        applicationMaterialsTextbookMappingMapper.delete(queryWrapper);
        
        // 批量删除应用素材
        return this.removeByIds(ids);
    }

    @Override
    public ApplicationMaterialsVO getApplicationMaterialsById(Long id, boolean includeTeachingMaterials) {
        if (id == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "应用素材ID不能为空");
        }
        
        // 查询应用素材详情
        ApplicationMaterialsVO applicationMaterialsVO = baseMapper.selectApplicationMaterialsById(id);
        if (applicationMaterialsVO == null) {
            throw new BusinessException(BusinessErrorEnum.NO_EXIT, "应用素材不存在");
        }
        
        // 是否需要关联教学素材信息
        if (includeTeachingMaterials) {
            List<ApplicationMaterialsDetailVO> teachingMaterials = applicationMaterialsMappingMapper.getTeachingMaterialsByApplicationId(id);
            applicationMaterialsVO.setTeachingMaterials(teachingMaterials);
        }
        
        return applicationMaterialsVO;
    }

    @Override
    public Page<ApplicationMaterialsVO> getApplicationMaterialsPage(ApplicationMaterialsPageParam param) {
        if (param == null) {
            param = new ApplicationMaterialsPageParam();
        }
        
        // 获取当前登录用户信息
        Long currentUserId = UserUtils.get() != null ? UserUtils.get().getId() : null;
        Integer currentUserType = UserUtils.get() != null ? UserUtils.get().getUserType() : null;
        
        // 数据权限控制：非管理员用户只能查询自己创建的素材
        if (currentUserType == null || currentUserType != 0) {
            if (currentUserId != null) {
                param.setCreator(currentUserId);
            }
        }
        
        // 处理分页参数
        if (param.getCurrent() == null || param.getCurrent() < 1) {
            param.setCurrent(1L);
        }
        if (param.getSize() == null || param.getSize() < 1) {
            param.setSize(10L);
        }
        
        // 预处理逻辑：处理未绑定模式下的目录递归筛选
        if (param.getOnlyUnbound() != null && param.getOnlyUnbound() && 
            param.getTextbookCatalogId2() != null && param.getTextbookCatalogId2() > 0) {
            
            // 创建一个Set来存储目标目录ID
            Set<Long> targetCatalogIds = new HashSet<>();
            
            // 先加入当前目录ID
            targetCatalogIds.add(param.getTextbookCatalogId2());
            
            // 查询当前目录详情
            TextbookCatalog currentCatalog = textbookCatalogMapper.selectById(param.getTextbookCatalogId2());
            
            // 判断当前目录的级别，如果是2级及以上则向上递归查找父目录
            if (currentCatalog != null && currentCatalog.getCatalogLevel() != null && currentCatalog.getCatalogLevel() >= 2) {
                // 循环向上查找父节点，直到父节点的catalogLevel < 2为止
                Long parentId = currentCatalog.getFatherCatalogId();
                TextbookCatalog parentCatalog = parentId != null ? textbookCatalogMapper.selectById(parentId) : null;
                
                while (parentCatalog != null && parentCatalog.getCatalogLevel() != null && parentCatalog.getCatalogLevel() >= 1) {
                    targetCatalogIds.add(parentCatalog.getId());
                    parentId = parentCatalog.getFatherCatalogId();
                    parentCatalog = parentId != null ? textbookCatalogMapper.selectById(parentId) : null;
                }
            }
            
            // 将Set转换为List并设置到参数中
            param.setTargetCatalogIds(new ArrayList<>(targetCatalogIds));
        }
        
        // 创建分页对象
        Page<ApplicationMaterialsVO> page = new Page<>(param.getCurrent(), param.getSize());
        
        // 调用Mapper层的分页查询方法
        page = baseMapper.selectApplicationMaterialsPage(page, param);
        
        // 为每个应用素材填充关联的教学素材列表
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            for (ApplicationMaterialsVO vo : page.getRecords()) {
                if (vo.getId() != null) {
                    List<ApplicationMaterialsDetailVO> teachingMaterials = 
                        applicationMaterialsMappingMapper.getTeachingMaterialsByApplicationId(vo.getId());
                    vo.setTeachingMaterials(teachingMaterials);
                }
                
                // 设置章节名称，先取textbookCatalogId对应的章节名，没有的话再取textbookCatalogId2
                String textbookCatalogName = vo.getTextbookCatalogName();
                // 去除HTML标签，只保留纯文本
                if (textbookCatalogName != null) {
                    textbookCatalogName = com.upc.utils.HtmlUtils.stripHtml(textbookCatalogName);
                }
                vo.setTextbookCatalogName(textbookCatalogName);
            }
        }
        
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean relateTeachingMaterials(Long applicationMaterialId, List<Long> teachingMaterialIds) {
        if (applicationMaterialId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "应用素材ID不能为空");
        }
        
        if (CollectionUtils.isEmpty(teachingMaterialIds)) {
            return true; // 空列表不做处理，返回成功
        }
        
        // 检查应用素材是否存在
        ApplicationMaterials existingMaterial = this.getById(applicationMaterialId);
        if (existingMaterial == null) {
            throw new BusinessException(BusinessErrorEnum.NO_EXIT, "应用素材不存在");
        }
        
        // 获取当前登录用户
        Long currentUserId = UserUtils.get() != null ? UserUtils.get().getId() : null;
        LocalDateTime now = LocalDateTime.now();
        
        // 构建映射关系
        List<ApplicationMaterialsMapping> mappings = new ArrayList<>();
        for (int i = 0; i < teachingMaterialIds.size(); i++) {
            Long teachingMaterialId = teachingMaterialIds.get(i);
            if (teachingMaterialId == null) {
                continue;
            }
            
            ApplicationMaterialsMapping mapping = new ApplicationMaterialsMapping();
            mapping.setApplicationMaterialId(applicationMaterialId);
            mapping.setTeachingMaterialId(teachingMaterialId);
            mapping.setSequence(i + 1); // 按照传入顺序设置序号，从1开始
            mapping.setCreator(currentUserId);
            mapping.setAddDatetime(now);
            mapping.setOperator(currentUserId);
            mapping.setOperationDatetime(now);
            
            mappings.add(mapping);
        }
        
        if (mappings.isEmpty()) {
            return true;
        }
        
        // 批量插入映射关系
        return applicationMaterialsMappingMapper.batchInsert(mappings) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeTeachingMaterials(Long applicationMaterialId, List<Long> teachingMaterialIds) {
        if (applicationMaterialId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "应用素材ID不能为空");
        }
        
        if (CollectionUtils.isEmpty(teachingMaterialIds)) {
            return true; // 空列表不做处理，返回成功
        }
        
        // 查询现有关联
        List<ApplicationMaterialsMapping> existingMappings = applicationMaterialsMappingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApplicationMaterialsMapping>()
                        .eq(ApplicationMaterialsMapping::getApplicationMaterialId, applicationMaterialId)
                        .in(ApplicationMaterialsMapping::getTeachingMaterialId, teachingMaterialIds)
        );
        
        if (CollectionUtils.isEmpty(existingMappings)) {
            return true; // 没有找到关联记录，返回成功
        }
        
        // 批量删除关联
        List<Long> mappingIds = existingMappings.stream()
                .map(ApplicationMaterialsMapping::getId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
                
        return applicationMaterialsMappingMapper.deleteBatchIds(mappingIds) > 0;
    }

    @Override
    public List<ApplicationMaterials> listByTextbookId(Long textbookId) {
        if (textbookId == null) {
            return Collections.emptyList();
        }

        // 1. 先查关联表，拿到所有应用素材ID
        List<ApplicationMaterialsTextbookMapping> mappings =
                applicationMaterialsTextbookMappingMapper.selectList(
                        new LambdaQueryWrapper<ApplicationMaterialsTextbookMapping>()
                                .eq(ApplicationMaterialsTextbookMapping::getTextbookId, textbookId)
                );
        if (CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyList();
        }

        List<Long> appIds = mappings.stream()
                .map(ApplicationMaterialsTextbookMapping::getApplicationMaterialId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (appIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 再查应用素材主表
        return this.listByIds(appIds);
    }
}
