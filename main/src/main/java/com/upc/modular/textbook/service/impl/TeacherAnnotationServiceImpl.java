package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.impl.TeacherServiceImpl;
import com.upc.modular.textbook.entity.TeacherAnnotation;
import com.upc.modular.textbook.mapper.TeacherAnnotationMapper;
import com.upc.modular.textbook.param.TeacherAnnotationReturnParam;
import com.upc.modular.textbook.service.ITeacherAnnotationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mjh
 * @since 2025-09-02
 */
@Service
public class TeacherAnnotationServiceImpl extends ServiceImpl<TeacherAnnotationMapper, TeacherAnnotation> implements ITeacherAnnotationService {

    @Autowired
    TeacherServiceImpl teacherService;

    public Long insertTeacherAnnotation(TeacherAnnotation param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getContent()) || ObjectUtils.isEmpty(param.getTextbookId()) || ObjectUtils.isEmpty(param.getCatalogueId()))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        // 前端要求：请求参数不需要教师id，后续也不用校验权限，因为目前教学活动思政这些都没有限制非本人不能修改
//        Long userId = UserUtils.get().getId();
//        Teacher teacher = teacherService.getOne(new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, userId));
//        if (ObjectUtils.isEmpty(teacher))
//            throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR,"，教师不存在");
//        param.setTeacherId(teacher.getId());
        param.setId(null);
        if (this.save(param))
            return param.getId();
        throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR,"添加教师批注失败");
    }

    @Override
    public TeacherAnnotationReturnParam getTeacherAnnotation(Long id) {
        TeacherAnnotation teacherAnnotation = this.getById(id);
        TeacherAnnotationReturnParam returnParam = new TeacherAnnotationReturnParam();
        BeanUtils.copyProperties(teacherAnnotation, returnParam);
        if (ObjectUtils.isNotEmpty(teacherAnnotation.getTeacherId())) {
            Teacher teacher = teacherService.getById(teacherAnnotation.getTeacherId());
            if (ObjectUtils.isNotEmpty(teacher) && ObjectUtils.isNotEmpty(teacher.getName()))
                returnParam.setTeacherName(teacher.getName());
        }

        return returnParam;
    }
}
