package com.upc.modular.datastatistics.service;

import com.upc.modular.datastatistics.controller.param.*;
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

    // ========== 智能化分析功能 ==========

    /**
     * 班级章节掌握情况分析
     * @param classId 班级ID
     * @param textbookId 教材ID
     * @return 班级章节掌握情况
     */
    ClassChapterMasteryReturnParam analyzeClassChapterMastery(Long classId, Long textbookId);

    /**
     * 班级分析报告
     * @param classId 班级ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 班级分析报告
     */
    ClassAnalysisReturnParam generateClassAnalysisReport(Long classId, String startTime, String endTime);

    /**
     * 班级学习行为分析
     * @param classId 班级ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 班级学习行为分析
     */
    ClassBehaviorAnalysisReturnParam analyzeClassLearningBehavior(Long classId, String startTime, String endTime);
}