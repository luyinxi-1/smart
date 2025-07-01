package com.upc.modular.teacher.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.teacher.controller.param.TeacherPageSearchParam;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teacher.service.ITeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements ITeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public void insert(Teacher teacher) {
        if (ObjectUtils.isEmpty(teacher.getIdcard())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户身份证号不为空");
        }
        MyLambdaQueryWrapper<Teacher> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Teacher::getIdcard, teacher.getIdcard());
        List<Teacher> teachers = teacherMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(teachers)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户已存在");
        }
        this.save(teacher);
    }

    @Override
    public void deleteDictItemByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 查询教师记录
        List<Teacher> teachers = teacherMapper.selectBatchIds(idList);
        if (ObjectUtils.isEmpty(teachers)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到对应的教师记录");
        }

        // 提取对应的 userId 并删除用户
        List<Long> userIdList = teachers.stream()
                .map(Teacher::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ObjectUtils.isNotEmpty(userIdList)) {
            sysUserService.batchDelete(userIdList);
        }

        // 删除教师记录
        this.removeBatchByIds(idList);
    }

    @Override
    public Page<Teacher> getPage(TeacherPageSearchParam param) {
        Page<Teacher> page = new Page<>(param.getCurrent(), param.getSize());
        MyLambdaQueryWrapper<Teacher> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();

        // 条件过滤（模糊或精确匹配）
        lambdaQueryWrapper
                .like(ObjectUtils.isNotEmpty(param.getName()), Teacher::getName, param.getName())
                .like(ObjectUtils.isNotEmpty(param.getIdentityId()), Teacher::getIdentityId, param.getIdentityId())
                .like(ObjectUtils.isNotEmpty(param.getIdcard()), Teacher::getIdcard, param.getIdcard())
                .eq(ObjectUtils.isNotEmpty(param.getGender()), Teacher::getGender, param.getGender())
                .like(ObjectUtils.isNotEmpty(param.getNationality()), Teacher::getNationality, param.getNationality())
                .like(ObjectUtils.isNotEmpty(param.getPosition()), Teacher::getPosition, param.getPosition())
                .like(ObjectUtils.isNotEmpty(param.getProfessionalTitle()), Teacher::getProfessionalTitle, param.getProfessionalTitle())
                .orderBy(true, param.getIsAsc() == 1, Teacher::getAddDatetime);;

        // 分页查询
        return this.page(page, lambdaQueryWrapper);
    }

}
