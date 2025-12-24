package com.upc.modular.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.course.entity.Course;
import com.upc.modular.course.entity.CourseClassList;
import com.upc.modular.course.mapper.CourseClassListMapper;
import com.upc.modular.course.mapper.CourseMapper;
import com.upc.modular.datastatistics.mapper.StudentDataStatisticsMapper;
import com.upc.modular.group.controller.param.pageGroup;
import com.upc.modular.group.controller.param.pageGroupVo;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.entity.UserClassList;
import com.upc.modular.group.mapper.GroupMapper;
import com.upc.modular.group.service.IGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.utils.InstitutionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {
    @Autowired
    private UserClassListServiceImpl userClassListService;

    @Autowired
    private IInstitutionService institutionService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ITeacherService teacherService;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private CourseClassListMapper courseClassListMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private StudentDataStatisticsMapper studentDataStatisticsMapper;

    @Override
    public Page<pageGroupVo> selectgetByidPage(pageGroup dictType) {
        Page<Group> page = new Page<>(dictType.getCurrent(), dictType.getSize());
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(dictType.getId() != null, Group::getId, dictType.getId())
                .eq(dictType.getGrade() != null, Group::getGrade, dictType.getGrade())
                .eq(dictType.getStatus() != null, Group::getStatus, dictType.getStatus())
                .eq(dictType.getClassStatus() != null, Group::getClassStatus, dictType.getClassStatus())
                .like(!StringUtils.isEmpty(dictType.getName()), Group::getName, dictType.getName())
                .eq(Group::getStatus, 1);

        // 添加组织ID查询条件
        if (dictType.getInstitutionId() != null) {
            // 获取指定组织及其所有下级组织的ID列表
            List<Institution> allInstitutions = institutionService.list();
            List<Long> institutionIds = InstitutionUtil.getAllSubInstitutionIds(dictType.getInstitutionId(), allInstitutions);
            queryWrapper.in(Group::getInstitutionId, institutionIds);
        }

        // 修改排序方式：按照修改时间降序排列
        queryWrapper.orderByDesc(Group::getOperationDatetime);

        Page<Group> groupPage = baseMapper.selectPage(page, queryWrapper);

        Page<pageGroupVo> voPage = new Page<>(groupPage.getCurrent(), groupPage.getSize(), groupPage.getTotal());
        List<pageGroupVo> voRecords = new ArrayList<>();

        // 收集所有教师ID以便批量查询
        Set<Long> teacherIds = groupPage.getRecords().stream()
                .map(Group::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询教师信息
        Map<Long, String> teacherNameMap = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            LambdaQueryWrapper<Teacher> teacherQueryWrapper = new LambdaQueryWrapper<>();
            teacherQueryWrapper.in(Teacher::getId, teacherIds);
            List<Teacher> teachers = teacherService.list(teacherQueryWrapper);
            teacherNameMap = teachers.stream()
                    .collect(Collectors.toMap(Teacher::getId, Teacher::getName));
        }

        // 收集所有创建人ID以便批量查询
        Set<Long> creatorIds = groupPage.getRecords().stream()
                .map(Group::getCreator)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询创建人信息（直接从用户表获取nickname）
        Map<Long, String> creatorNameMap = new HashMap<>();
        if (!creatorIds.isEmpty()) {
            LambdaQueryWrapper<SysTbuser> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.in(SysTbuser::getId, creatorIds);
            List<SysTbuser> users = sysUserService.list(userQueryWrapper);
            creatorNameMap = users.stream()
                    .filter(user -> user.getNickname() != null)
                    .collect(Collectors.toMap(SysTbuser::getId, SysTbuser::getNickname));
        }

        for (Group group : groupPage.getRecords()) {
            pageGroupVo vo = new pageGroupVo();
            // 复制基础属性
            BeanUtils.copyProperties(group, vo);

            // 设置教师姓名
            if (group.getTeacherId() != null) {
                vo.setTeacherName(teacherNameMap.get(group.getTeacherId()));
            }

            // 设置创建人姓名（从用户表nickname获取）
            if (group.getCreator() != null) {
                vo.setCreatorName(creatorNameMap.get(group.getCreator()));
            }

            voRecords.add(vo);
        }

        // 4. 将处理好的列表放入新的分页对象中
        voPage.setRecords(voRecords);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByIdStudents(Group group) {
        if (group == null || group.getId() == null) {
            return false;
        }

        // 1. 获取更新前的班级信息，用于定位对应的组织
        Group oldGroup = this.getById(group.getId());
        if(oldGroup == null || !Integer.valueOf(1).equals(oldGroup.getStatus())) {
            // 如果班级不存在或状态不是1，则不允许更新
            return false;
        }

        // 2. 查找对应的组织
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        // 使用更新前的 名称 和 父组织ID 来定位
        wrapper.eq(Institution::getInstitutionName, oldGroup.getName())
                .eq(Institution::getFatherInstitutionId, oldGroup.getInstitutionId());
        Institution institutionToUpdate = institutionService.getOne(wrapper);

        // 3. 如果找到了对应的组织，则更新它
        if (institutionToUpdate != null) {
            institutionToUpdate.setInstitutionName(group.getName()); // 更新名称
            institutionToUpdate.setFatherInstitutionId(group.getInstitutionId()); // 更新父级ID
            institutionToUpdate.setIntroduction(group.getRemark()); // 更新介绍
            institutionService.updateById(institutionToUpdate);
        }

        if (group.getDefaultClassroom() == null) {
            group.setDefaultClassroom(oldGroup.getDefaultClassroom());
        }
        if (group.getAdmissionDate() == null) {
            group.setAdmissionDate(oldGroup.getAdmissionDate());
        }
        if (group.getGraduationDate() == null) {
            group.setGraduationDate(oldGroup.getGraduationDate());
        }

        // 4. 更新班级表
        return this.updateById(group);
    }


    @Override
    public Group getByIdStudents(Long groupId) {
        if (groupId == null || groupId <= 0) {
            return null;
        }
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Group::getId, groupId)
                .eq(Group::getStatus, 1);
        return this.getOne(queryWrapper);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelectStudents(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return false;
        }
        // 1. 根据ID列表查出所有要删除的班级实体
        List<Group> groupsToDelete = this.listByIds(idList);
        if (CollectionUtils.isEmpty(groupsToDelete)) {
            return true; // 列表为空，认为删除成功
        }

        // 2. 准备查询条件，找到所有对应的组织
        List<Long> institutionIdsToDelete = new ArrayList<>();
        for (Group group : groupsToDelete) {
            LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Institution::getInstitutionName, group.getName())
                    .eq(Institution::getFatherInstitutionId, group.getInstitutionId());
            List<Institution> institutions = institutionService.list(wrapper);
            institutionIdsToDelete.addAll(institutions.stream().map(Institution::getId).collect(Collectors.toList()));
        }

        // 3. 如果找到了要删除的组织，执行删除
        if (!institutionIdsToDelete.isEmpty()) {
            institutionService.removeByIds(institutionIdsToDelete);
        }

        // 4. 删除班级表中的数据
        int deletedRows = baseMapper.deleteBatchIds(idList);
        return deletedRows > 0;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertstudentlist(List<Group> groupsList) {
        if (CollectionUtils.isEmpty(groupsList)) {
            return false;
        }

        for (Group group : groupsList) {
            // 1. 保存班级信息到 group 表
            this.save(group); // mybatis-plus的save方法会自动将生成的主键回填到group对象中

            // 2. 创建一个新的 Institution 对象
            Institution institution = new Institution();
            institution.setInstitutionName(group.getName()); // 机构名称 = 班级名称
            institution.setFatherInstitutionId(group.getInstitutionId()); // 父级ID = 班级所属组织ID
            institution.setIntroduction(group.getRemark()); // 介绍 = 班级备注

            // 3. 保存组织信息到 institution 表
            institutionService.save(institution);
        }

        return true;
    }

    @Override
    public Map<String, Long> getUserTypeCountByClassId(Long groupId) {
        // --- 修改点：先检查班级是否存在且状态为1 ---
        Group group = this.getByIdStudents(groupId); // 复用带有status检查的查询方法
        if (group == null) {
            // 如果班级无效或不存在，返回空结果
            return Collections.emptyMap();
        }

        MyLambdaQueryWrapper<UserClassList> listMyLambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        listMyLambdaQueryWrapper.eq(UserClassList::getClassId, groupId)
                .select(UserClassList::getType);


        List<UserClassList> userList = userClassListService.list(listMyLambdaQueryWrapper);

        Map<Integer, Long> typeCountMap = userList.stream()
                .collect(Collectors.groupingBy(UserClassList::getType, Collectors.counting()));

        Map<String, Long> result = new HashMap<>();
        result.put("管理员", typeCountMap.getOrDefault(0, 0L));
        result.put("学生", typeCountMap.getOrDefault(1, 0L));
        result.put("老师", typeCountMap.getOrDefault(2, 0L));
        return result;
    }
    
    @Override
    public List<Group> getGroupsByTeacherUserId(Long userId) {
        // 检查用户是否为管理员（userType == 0 表示管理员）
        SysTbuser currentUser = sysUserService.getById(userId);
        if (currentUser != null && currentUser.getUserType() != null && currentUser.getUserType() == 0) {
            // 如果是管理员，返回所有班级
            return this.list(new LambdaQueryWrapper<Group>().eq(Group::getStatus, 1));
        }
        
        // 1. 根据用户ID获取教师ID
        Long teacherId = teacherMapper.getTeacherIdByUserId(userId);
        if (teacherId == null) {
            return new ArrayList<>(); // 如果找不到对应的教师，返回空列表
        }
        
        // 2. 创建用于存储班级ID的Set，以实现去重
        Set<Long> groupIds = new HashSet<>();
        
        // 3. 根据教师ID在group表中查找对应的班级
        LambdaQueryWrapper<Group> groupQueryWrapper = new LambdaQueryWrapper<>();
        groupQueryWrapper.eq(Group::getTeacherId, teacherId)
                .eq(Group::getStatus, 1); // 只查找状态为1的班级
        List<Group> groupsByTeacher = this.list(groupQueryWrapper);
        
        // 4. 收集这些班级的ID
        groupsByTeacher.forEach(group -> groupIds.add(group.getId()));
        
        // 5. 根据教师ID查找该教师对应的课程ID
        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.eq(Course::getTeacherId, teacherId);
        List<Course> courses = courseMapper.selectList(courseQueryWrapper);
        List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());
        
        // 6. 如果存在课程，根据课程ID查找对应的班级ID
        if (!courseIds.isEmpty()) {
            // 通过course_class_list表查找班级ID
            LambdaQueryWrapper<CourseClassList> courseClassListQueryWrapper = new LambdaQueryWrapper<>();
            courseClassListQueryWrapper.in(CourseClassList::getCourseId, courseIds);
            List<CourseClassList> courseClassLists = courseClassListMapper.selectList(courseClassListQueryWrapper);
            
            // 收集班级ID
            courseClassLists.forEach(courseClassList -> groupIds.add(courseClassList.getClassId()));
        }
        
        // 7. 根据收集到的班级ID查找所有班级信息
        if (!groupIds.isEmpty()) {
            LambdaQueryWrapper<Group> finalGroupQueryWrapper = new LambdaQueryWrapper<>();
            finalGroupQueryWrapper.in(Group::getId, groupIds)
                    .eq(Group::getStatus, 1); // 只查找状态为1的班级
            return this.list(finalGroupQueryWrapper);
        }
        
        return groupsByTeacher;
    }

    @Override
    public Map<String, Object> getClassStatisticsByUserId(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 获取班级列表（管理员获取所有班级，教师只获取筛选后的班级）
        List<Group> groups = getGroupsByTeacherUserId(userId);
        
        // 1. 班级数量
        int classCount = groups.size();
        result.put("classCount", classCount);
        
        // 2. 班级总人数
        Set<Long> studentUserIds = new HashSet<>();
        if (!groups.isEmpty()) {
            List<Long> groupIds = groups.stream()
                    .map(Group::getId)
                    .collect(Collectors.toList());
            
            // 查询这些班级中的所有学生
            LambdaQueryWrapper<Student> studentQueryWrapper = new LambdaQueryWrapper<>();
            studentQueryWrapper.in(Student::getClassId, groupIds);
            List<Student> students = studentMapper.selectList(studentQueryWrapper);
            
            // 收集学生用户ID
            students.forEach(student -> studentUserIds.add(student.getUserId()));
        }
        result.put("studentCount", studentUserIds.size());
        
        // 3. 班级总阅读时长(小时) - 使用批量查询优化性能
        double totalReadingHours = 0;
        if (!studentUserIds.isEmpty()) {
            // 批量查询所有学生的阅读时长
            for (Long studentUserId : studentUserIds) {
                // 获取每个学生的阅读时长并累加(以秒为单位)
                Long studentReadingTime = studentDataStatisticsMapper.getStudentReadingTimeByUserId(studentUserId);
                if (studentReadingTime != null) {
                    totalReadingHours += studentReadingTime;
                }
            }
            // 将秒转换为小时
            totalReadingHours = totalReadingHours / 3600;
        }
        result.put("readingCount", totalReadingHours);
        
        return result;
    }
}