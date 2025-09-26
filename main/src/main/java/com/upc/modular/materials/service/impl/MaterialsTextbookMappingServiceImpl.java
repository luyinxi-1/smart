package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
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
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    @Override
    public Long insertMapping(Long textbookId, Long materialId, String chapterName, Integer chapterId) {
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




















