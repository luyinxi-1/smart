package com.upc.modular.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.student.controller.param.pageStudent;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.student.service.IStudentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {


    @Override
    public boolean insertstudentlist(List<Student> studentList) {
        // 1. 参数校验：检查列表是否为空
        if (CollectionUtils.isEmpty(studentList)) {
            return false;
        }
        // 2. 填充公共字段
        Long systemOperatorId = -1L;
        LocalDateTime now = LocalDateTime.now();

        // 遍历列表，为每个对象手动设置创建者和创建时间
        for (Student student : studentList) {
            student.setCreator(systemOperatorId);
            student.setAddDatetime(now);
        }
        return this.saveBatch(studentList);
    }

    @Override
    public boolean batchDelectStudents(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return false;
        }
        int deletedRows = baseMapper.deleteBatchIds(idList);
        return deletedRows > 0;
    }

    @Override
    public Student getByIdStudents(Long studentId) {
        if (studentId == null || studentId <= 0) {
            return null;
        }
        return this.getById(studentId);
    }

    @Override
    public boolean updateByIdStudents(Student student) {
        if (student == null || student.getId() == null) {
            return false;
        }
        // 手动填充操作者和操作时间字段
        // 同样，我们使用一个固定的ID（-1L）代表“系统”或“未知操作者”
        Long systemOperatorId = -1L;
        student.setOperator(systemOperatorId);
        student.setOperationDatetime(LocalDateTime.now());
        return this.updateById(student);
    }

    @Override
    public Page<Student> selectgetByidPage(pageStudent dictType) {
        // 从传入的 dictType 参数中获取分页信息 (current, size)
        Page<Student> page = new Page<>(dictType.getCurrent(), dictType.getSize());
        // 2. 创建查询条件构造器
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        // 3. 根据传入的参数动态构建查询条件
        queryWrapper
                // 精确匹配查询
                .eq(dictType.getId() != null, Student::getId, dictType.getId())
                .eq(dictType.getUserId() != null, Student::getUserId, dictType.getUserId())
                .eq(!StringUtils.isEmpty(dictType.getIdentityId()), Student::getIdentityId, dictType.getIdentityId())
                .eq(dictType.getClassId() != null, Student::getClassId, dictType.getClassId())
                .eq(dictType.getStatus() != null, Student::getStatus, dictType.getStatus())
                .eq(!StringUtils.isEmpty(dictType.getGender()), Student::getGender, dictType.getGender())
                // 模糊匹配查询 (like)
                .like(!StringUtils.isEmpty(dictType.getName()), Student::getName, dictType.getName())
                .like(!StringUtils.isEmpty(dictType.getCollege()), Student::getCollege, dictType.getCollege());
        // 4. 添加默认排序规则，例如按创建时间降序
        queryWrapper.orderByDesc(Student::getAddDatetime);
        // 5. 执行分页查询并返回结果
        return baseMapper.selectPage(page, queryWrapper);
    }
}
