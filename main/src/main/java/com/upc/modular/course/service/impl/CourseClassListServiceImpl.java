package com.upc.modular.course.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.course.controller.param.ClassInfoReturnParam;
import com.upc.modular.course.controller.param.GetMyCourseReturnParam;
import com.upc.modular.course.controller.param.GetMyCourseSearchParam;
import com.upc.modular.course.entity.CourseClassList;
import com.upc.modular.course.entity.CourseTextbookList;
import com.upc.modular.course.mapper.CourseClassListMapper;
import com.upc.modular.course.service.ICourseClassListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.course.service.ICourseService;
import com.upc.modular.course.service.ICourseTextbookListService;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.service.IGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CourseClassListServiceImpl extends ServiceImpl<CourseClassListMapper, CourseClassList> implements ICourseClassListService {

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private CourseClassListMapper courseClassListMapper;

    @Autowired
    private ICourseTextbookListService courseTextbookListService;

    @Override
    public void associateClasses(Long courseId, List<Long> classIds) {
        // 校验 courseId 是否存在
        if (courseService.getById(courseId) == null) {
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "课程 ID " + courseId + " 不存在"
            );
        }

        // 校验 classIds 列表
        if (CollectionUtils.isEmpty(classIds)) {
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "classIds 列表不能为空"
            );
        }
        List<Group> groups = groupService.listByIds(classIds);
        List<Long> exists = groups.stream()
                .map(Group::getId)
                .collect(Collectors.toList());
        // 找出传入里不存在的那些
        List<Long> missing = classIds.stream()
                .filter(id -> !exists.contains(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "以下班级 ID 不存在：" + missing
            );
        }

        // 先删除该课程已有的所有关联关系
        LambdaQueryWrapper<CourseClassList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseClassList::getCourseId, courseId);
        this.remove(wrapper);

        // 如果新的班级列表不为空，则插入新的关联关系
        if (!CollectionUtils.isEmpty(classIds)) {
            List<CourseClassList> newRelations = classIds.stream()
                    .map(classId -> {
                        CourseClassList relation = new CourseClassList();
                        relation.setCourseId(courseId);
                        relation.setClassId(classId);
                        relation.setCreator(UserUtils.get().getId());
                        return relation;
                    })
                    .collect(Collectors.toList());
            this.saveBatch(newRelations);
        }
    }

    @Override
    public List<ClassInfoReturnParam> getClassesByCourse(Long courseId) {
        // 查当前课程所有关联的班级id
        List<Long> classIds = courseClassListMapper.selectList(
                new LambdaQueryWrapper<CourseClassList>()
                        .eq(CourseClassList::getCourseId, courseId))
                .stream()
                .map(CourseClassList::getClassId)
                .collect(Collectors.toList());

        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量拿班级详情
        List<Group> groups = groupService.listByIds(classIds);

        // 3. 构建返回 DTO，顺便统计学生数
        return groups.stream().map(g -> {
            ClassInfoReturnParam dto = new ClassInfoReturnParam();
            dto.setName(g.getName());
            // 用 GroupService里面的接口统计
            Map<String, Long> result = groupService.getUserTypeCountByClassId(g.getId());
            Long studentCount = result.get(1);
            dto.setStudentCount(studentCount);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<GetMyCourseReturnParam> getMyCourse(GetMyCourseSearchParam param) {
        if (ObjectUtils.isEmpty(UserUtils.get().getId())) {
            return null;
        }
        if (ObjectUtils.isEmpty(UserUtils.get().getUserType())) {
            return null;
        }
        List<GetMyCourseReturnParam> resultList = new ArrayList<>();
        Long id = UserUtils.get().getId();
        if (UserUtils.get().getUserType() == 1) {
            resultList = courseClassListMapper.getMyCourseTeacher(id, param);
        }
        if (UserUtils.get().getUserType() == 2) {
            resultList = courseClassListMapper.getMyCourseStudent(id, param);
        }
        if (UserUtils.get().getUserType() == 0) {
            resultList = courseClassListMapper.getMyCourseAdmin(id, param);
        }
        for (GetMyCourseReturnParam params : resultList) {
            if (ObjectUtils.isEmpty(params.getId())) {
                MyLambdaQueryWrapper<CourseTextbookList> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(CourseTextbookList::getCourseId, params.getId());
                long count = courseTextbookListService.count(lambdaQueryWrapper);
                params.setTextbookNumber(count);
                List<ClassInfoReturnParam> classesByCourse = this.getClassesByCourse(params.getId());
                params.setGroupList(classesByCourse);
            }
        }
        return resultList;
    }
}
