package com.upc.modular.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.teacher.dto.TeacherLogPageSearchParam;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.entity.TeacherLog;
import com.upc.modular.teacher.mapper.TeacherLogMapper;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teacher.service.ITeacherLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-08-21
 */
@Service
public class TeacherLogServiceImpl extends ServiceImpl<TeacherLogMapper, TeacherLog> implements ITeacherLogService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private TeacherLogMapper teacherLogMapper;
    @Override
    public void inserTeacherLog(TeacherLog param) {
        Long teacherId = param.getTeacherId();

        // 外键需要判断是否存在
        LambdaQueryWrapper<Teacher> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.eq(Teacher::getId,teacherId);
        boolean isTeacherExists = teacherMapper.exists(queryWrapper0);
        if (!isTeacherExists) {
            throw new RuntimeException("ID为 " + teacherId + " 的教师不存在！");
        }else {
            if(param.getUserId() == null){
                Long userId = teacherMapper.selectOne(queryWrapper0).getUserId();
                param.setUserId(userId);
            }
        }

        this.save(param);
    }

    @Override
    public Page<TeacherLog> selectTeacherLogPage(TeacherLogPageSearchParam param) {
        Page<TeacherLog> page = new Page<>(param.getCurrent(), param.getSize());
        return teacherLogMapper.selectTeacherLogPage(page, param);
    }
}
