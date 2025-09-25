package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.impl.TeacherServiceImpl;
import com.upc.modular.textbook.entity.TeacherAnnotation;
import com.upc.modular.textbook.mapper.TeacherAnnotationMapper;
import com.upc.modular.textbook.param.TeacherAnnotationPageSearchParam;
import com.upc.modular.textbook.param.TeacherAnnotationReturnParam;
import com.upc.modular.textbook.service.ITeacherAnnotationService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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
 * @since 2025-09-02
 */
@Service
public class TeacherAnnotationServiceImpl extends ServiceImpl<TeacherAnnotationMapper, TeacherAnnotation> implements ITeacherAnnotationService {

    @Autowired
    TeacherServiceImpl teacherService;

    @Autowired
    private ISysUserService sysUserService;


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
        throw new BusinessException(BusinessErrorEnum.UNKNOWN_ERROR, "添加教师批注失败");
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

    @Override
    public Page<TeacherAnnotationReturnParam> getTeacherAnnotationPage(TeacherAnnotationPageSearchParam param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getTextbookId()))
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材id不能为空");
        List<TeacherAnnotation> annotationList = this.list(new LambdaQueryWrapper<TeacherAnnotation>().eq(TeacherAnnotation::getTextbookId, param.getTextbookId()));
        List<Long> teacherIdList = annotationList.stream().filter(annotation -> annotation.getTeacherId() != null).map(TeacherAnnotation::getTeacherId).collect(Collectors.toList());

        List<Teacher> teacherList = new ArrayList<>();
        if (!teacherIdList.isEmpty()) {
            teacherList = teacherService.list(new LambdaQueryWrapper<Teacher>().in(Teacher::getId, teacherIdList));
        }

        Map<Long, Teacher> teacherIdNameMap = teacherList.stream()
                .collect(Collectors.toMap(
                        Teacher::getId,
                        t -> t
                ));

        List<Long> teacherUserIdList = teacherList.stream().map(Teacher::getUserId).collect(Collectors.toList());

        Map<Long, SysTbuser> userIdUserPictureMap = new HashMap<>();
        if (!teacherUserIdList.isEmpty()) {
            userIdUserPictureMap = sysUserService.list(
                            new LambdaQueryWrapper<SysTbuser>()
                                    .in(SysTbuser::getId, teacherUserIdList)
                                    .select(SysTbuser::getId, SysTbuser::getUserPicture))
                    .stream()
                    .collect(Collectors.toMap(
                            SysTbuser::getId,
                            sysUser -> sysUser
                    ));
        }


        List<TeacherAnnotationReturnParam> returnParamList = new ArrayList<>();
        for (TeacherAnnotation annotation : annotationList) {
            TeacherAnnotationReturnParam returnParam = new TeacherAnnotationReturnParam();
            BeanUtils.copyProperties(annotation, returnParam);
            returnParam.setTeacherName(teacherIdNameMap.get(annotation.getTeacherId()).getName());
            returnParam.setTeacherPhoto(userIdUserPictureMap.get(teacherIdNameMap.get(annotation.getTeacherId()).getUserId()).getUserPicture());
            returnParamList.add(returnParam);
        }

        Page<TeacherAnnotationReturnParam> resultPage = createPage(returnParamList, param.getCurrent(), param.getSize());

        return resultPage;
    }
}
