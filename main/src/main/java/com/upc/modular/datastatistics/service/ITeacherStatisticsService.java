package com.upc.modular.datastatistics.service;

import com.upc.modular.datastatistics.controller.param.TeacherStatisticsReturnParam;
import com.upc.modular.datastatistics.entity.TeacherStatistics;

/**
 * 教师统计Service接口
 */
public interface ITeacherStatisticsService {

    /**
     * 获取教师个人数据统计
     */
    TeacherStatisticsReturnParam getTeacherPersonalStatistics(Long teacherId);

    /**
     * 统计教师授课班级数量
     */
    Integer countTeacherClasses(Long teacherId);

    /**
     * 统计教师授课学生数量
     */
    Integer countTeacherStudents(Long teacherId);

    /**
     * 统计教师教材数量
     */
    Integer countTeacherTextbooks(Long teacherId);

    /**
     * 统计教师授课课程数量
     */
    Integer countTeacherCourses(Long teacherId);

    /**
     * 保存教师统计数据
     */
    void saveTeacherStatistics(TeacherStatistics statistics);
}