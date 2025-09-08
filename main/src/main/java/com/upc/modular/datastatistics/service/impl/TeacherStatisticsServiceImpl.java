package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.datastatistics.controller.param.TeacherStatisticsReturnParam;
import com.upc.modular.datastatistics.entity.TeacherStatistics;
import com.upc.modular.datastatistics.mapper.TeacherStatisticsMapper;
import com.upc.modular.datastatistics.service.ITeacherStatisticsService;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 教师统计Service实现类
 */
@Service
public class TeacherStatisticsServiceImpl extends ServiceImpl<TeacherStatisticsMapper, TeacherStatistics> implements ITeacherStatisticsService {

    @Autowired
    private TeacherStatisticsMapper teacherStatisticsMapper;

    @Autowired
    private ITeacherService teacherService;  // 注入教师服务

    @Override
    public TeacherStatisticsReturnParam getTeacherPersonalStatistics(Long teacherId) {
        TeacherStatisticsReturnParam result = new TeacherStatisticsReturnParam();
        result.setTeacherId(teacherId);

        // 获取教师姓名
        try {
            TeacherReturnVo teacherInfo = teacherService.getInformationByTeacherId(teacherId);
            if (teacherInfo != null) {
                result.setTeacherName(teacherInfo.getName());
            }
        } catch (Exception e) {
            // 如果获取教师信息失败，可以记录日志或设置默认值
            result.setTeacherName("未知教师");
        }

        // 统计各项数据
        result.setClassCount(countTeacherClasses(teacherId));
        result.setStudentCount(countTeacherStudents(teacherId));
        result.setTextbookCount(countTeacherTextbooks(teacherId));
        result.setCourseCount(countTeacherCourses(teacherId));
        result.setStatisticsDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return result;
    }

    @Override
    public Integer countTeacherClasses(Long teacherId) {
        return teacherStatisticsMapper.countTeacherClasses(teacherId);
    }

    @Override
    public Integer countTeacherStudents(Long teacherId) {
        return teacherStatisticsMapper.countTeacherStudents(teacherId);
    }

    @Override
    public Integer countTeacherTextbooks(Long teacherId) {
        return teacherStatisticsMapper.countTeacherTextbooks(teacherId);
    }

    @Override
    public Integer countTeacherCourses(Long teacherId) {
        return teacherStatisticsMapper.countTeacherCourses(teacherId);
    }


    @Override
    public void saveTeacherStatistics(TeacherStatistics statistics) {
        statistics.setStatisticsDate(LocalDateTime.now());
        save(statistics);
    }
}