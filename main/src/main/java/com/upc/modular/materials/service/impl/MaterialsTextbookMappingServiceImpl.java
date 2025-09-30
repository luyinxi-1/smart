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
import com.upc.modular.textbook.mapper.TextbookMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

 /*   @Override
    public Long insertMapping(Long textbookId, Long materialId, String chapterName, String  chapterId) {
        if (ObjectUtils.isEmpty(textbookId) || ObjectUtils.isEmpty(materialId) || ObjectUtils.isEmpty(chapterName) || ObjectUtils.isEmpty(chapterId))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "参数不能为空");
        MaterialsTextbookMapping mapping = new MaterialsTextbookMapping();
        mapping.setTextbookId(textbookId);
        mapping.setMaterialId(materialId);
        mapping.setChapterName(chapterName);
        mapping.setChapterId(chapterId);
        if (this.save(mapping)) {
            return mapping.getId(); // 返回新插入记录的ID
        }
        return null;
    }*/
 @Override
 public Long insertMapping(Long textbookId, Long materialId, String chapterName, String chapterId) {
     // 1. 参数非空校验 (保持不变)
     if (ObjectUtils.isEmpty(textbookId) || ObjectUtils.isEmpty(materialId) || ObjectUtils.isEmpty(chapterName) || ObjectUtils.isEmpty(chapterId))
         throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "参数不能为空");
     // 2. 唯一性校验：检查该素材是否已被绑定
     LambdaQueryWrapper<MaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
     // 查询条件：WHERE material_id = ?
     queryWrapper.eq(MaterialsTextbookMapping::getMaterialId, materialId);
     // 使用 getOne() 来查找是否已存在记录。相比 exists()，它可以获取到已存在的数据，用于生成更详细的错误提示。
     MaterialsTextbookMapping existingMapping = this.getOne(queryWrapper);
     if (existingMapping != null) {
         // 如果 existingMapping 不为 null，说明数据库中已存在该素材的绑定记录
         String errorMessage = String.format(
                 "该素材 (ID: %d) 已被绑定到教材 (ID: %d) 的章节 '%s',ID为 '%s'。一个素材只能绑定一次。",
                 materialId,
                 existingMapping.getTextbookId(),
                 existingMapping.getChapterName(),
                 existingMapping.getChapterId()

         );
         throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, errorMessage);
     }
     // 3. 如果校验通过（即素材未被绑定），则创建并保存新的绑定关系 (保持不变)
     MaterialsTextbookMapping mapping = new MaterialsTextbookMapping();
     mapping.setTextbookId(textbookId);
     mapping.setMaterialId(materialId);
     mapping.setChapterName(chapterName);
     mapping.setChapterId(chapterId);

     if (this.save(mapping)) {
         return mapping.getId();
     }
     return null;
 }
    /*@Override
    @Transactional(rollbackFor = Exception.class)
    public  List<Long> insertMappingBatch(List<MaterialsTextbookMappingDto> mappings) {
        // 1. 基本参数校验
        if (CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyList();
        }

        // 2. 检查请求列表内部是否存在重复的 (教材ID, 素材ID) 组合
        Set<String> uniquePairs = new HashSet<>();
        for (MaterialsTextbookMappingDto dto : mappings) {
            String pair = dto.getTextbookId() + ":" + dto.getMaterialId();
            if (!uniquePairs.add(pair)) {
                throw new RuntimeException("请求参数中存在重复的教材-素材关联: 教材ID " + dto.getTextbookId() + ", 素材ID " + dto.getMaterialId());
            }
        }
        // 3.批量校验所有涉及的教材ID是否存在
        Set<Long> textbookIds = mappings.stream().map(MaterialsTextbookMappingDto::getTextbookId).collect(Collectors.toSet());
        long existingTextbookCount = textbookMapper.selectCount(new LambdaQueryWrapper<Textbook>().in(Textbook::getId, textbookIds));
        if (existingTextbookCount != textbookIds.size()) {
            throw new RuntimeException("部分教材ID不存在，请检查后重试！");
        }

        // 4.批量校验所有涉及的素材ID是否存在
        Set<Long> materialIds = mappings.stream().map(MaterialsTextbookMappingDto::getMaterialId).collect(Collectors.toSet());
        long existingMaterialCount = teachingMaterialsMapper.selectCount(new LambdaQueryWrapper<TeachingMaterials>().in(TeachingMaterials::getId, materialIds));
        if (existingMaterialCount != materialIds.size()) {
            throw new RuntimeException("部分素材ID不存在，请检查后重试！");
        }
        // 5.批量校验数据库中是否已存在相同的关联关系
        LambdaQueryWrapper<MaterialsTextbookMapping> queryWrapper = new LambdaQueryWrapper<>();
        // 构造 (textbook_id = ? AND material_id = ?) OR (textbook_id = ? AND material_id = ?) ...
        queryWrapper.and(wrapper -> {
            for (int i = 0; i < mappings.size(); i++) {
                MaterialsTextbookMappingDto dto = mappings.get(i);
                wrapper.or(orWrapper -> orWrapper.eq(MaterialsTextbookMapping::getTextbookId, dto.getTextbookId())
                        .eq(MaterialsTextbookMapping::getMaterialId, dto.getMaterialId()));
            }
        });
        if (this.count(queryWrapper) > 0) {
            //throw new RuntimeException("数据库中已存在部分教材-素材关联，请勿重复添加！");
            List<MaterialsTextbookMapping> existing = this.list(queryWrapper);
            String existingDetails = existing.stream()
                    .map(e -> String.format("教材ID:%d-素材ID:%d-章节ID:%s,章节名称：%s", e.getTextbookId(), e.getMaterialId(),e.getChapterId(),e.getChapterName()))
                    .collect(Collectors.joining(", "));

            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "操作失败，以下关联关系已存在: " + existingDetails);

        }

        // 6. 数据转换与批量插入
        List<MaterialsTextbookMapping> entitiesToInsert = mappings.stream().map(dto -> {
            MaterialsTextbookMapping entity = new MaterialsTextbookMapping();
            entity.setTextbookId(dto.getTextbookId());
            entity.setMaterialId(dto.getMaterialId());
            entity.setChapterName(dto.getChapterName());
            entity.setChapterId(dto.getChapterId());
            return entity;
        }).collect(Collectors.toList());

        this.saveBatch(entitiesToInsert);

        // 7. 返回新生成的ID列表
        return entitiesToInsert.stream()
                .map(MaterialsTextbookMapping::getId)
                .collect(Collectors.toList());
    }*/

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> insertMappingBatch(List<MaterialsTextbookMappingDto> mappings) {
        // 1. 基本参数校验
        if (CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyList();
        }
        // 2. 【前置校验】
        // 2.1 检查请求列表内部是否存在重复的 (教材ID, 素材ID) 组合
/*        Set<String> uniquePairsInRequest = new HashSet<>();
        for (MaterialsTextbookMappingDto dto : mappings) {
            if (dto.getTextbookId() == null || dto.getMaterialId() == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "请求参数中存在空的教材ID或素材ID");
            }
            String pair = dto.getTextbookId() + ":" + dto.getMaterialId();
            if (!uniquePairsInRequest.add(pair)) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        String.format("请求参数中存在重复的绑定关系: 教材ID %d, 素材ID %d", dto.getTextbookId(), dto.getMaterialId()));
            }
        }*/
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
        // 收集本次操作要修改的所有章节ID
        Set<String> chapterIdsToModify = mappings.stream()
                .map(MaterialsTextbookMappingDto::getChapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 删除这些章节下的所有旧绑定关系
        if (!chapterIdsToModify.isEmpty()) {
            this.remove(new LambdaQueryWrapper<MaterialsTextbookMapping>()
                    .in(MaterialsTextbookMapping::getChapterId, chapterIdsToModify));
        }
        // 4. 【最终唯一性校验】
/*        // 检查请求中的 (教材ID, 素材ID) 组合是否已在本次操作范围之外的章节中存在
        LambdaQueryWrapper<MaterialsTextbookMapping> conflictCheckWrapper = new LambdaQueryWrapper<>();
        // 构造 (textbook_id = ? AND material_id = ?) OR (textbook_id = ? AND material_id = ?) ...
        conflictCheckWrapper.and(wrapper -> {
            for (MaterialsTextbookMappingDto dto : mappings) {
                wrapper.or(orWrapper -> orWrapper.eq(MaterialsTextbookMapping::getTextbookId, dto.getTextbookId())
                        .eq(MaterialsTextbookMapping::getMaterialId, dto.getMaterialId()));
            }
        });*/
        // 检查 material_id
        LambdaQueryWrapper<MaterialsTextbookMapping> conflictCheckWrapper = new LambdaQueryWrapper<>();
        conflictCheckWrapper.in(MaterialsTextbookMapping::getMaterialId, uniqueMaterialIdsInRequest);

        List<MaterialsTextbookMapping> conflictingBindings = this.list(conflictCheckWrapper);
        if (!conflictingBindings.isEmpty()) {
            String existingDetails = conflictingBindings.stream()
                    .map(e -> String.format("素材ID:%d，已被教材ID:%d，章节'%s'，章节ID：%s绑定", e.getMaterialId(), e.getTextbookId(), e.getChapterName(),e.getChapterId()))
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
            entity.setChapterId(dto.getChapterId());
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
}




















