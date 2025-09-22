package com.upc.modular.datastatistics.service;

import com.upc.modular.datastatistics.controller.param.ChapterMasteryVO;
import com.upc.modular.datastatistics.controller.param.VisitorCountDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ISystemStatisticsService {

    /**
     * 获取今日访问人数
     */
    Long getTodayVisitorCount();

    List<VisitorCountDTO> getStudentVisitorCountByTime(String startDate, String endDate);
    /**
     * 获取今日总学习时长
     */
    Long getTodayStudyDuration();

    // === 修改后的代码 ===
    /**
     * 根据时间范围获取总学习时长
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 总学习秒数
     */
    Long getStudyDurationByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    /**
     * 按时间统计总学习时长
     */
    // 按时间统计总学习时长
    //List<Map<String, Object>> getStudyDurationByTime(Map<String, Object> param);

    /**
     * 获取今日活跃人数
     */

/*
    // 今日活跃人数
    Long getTodayActiveUserCount();

    */
/**
     * 按时间统计活跃人数
     *//*

    // 按时间统计活跃人数
    List<Map<String, Object>> getActiveUserCountByTime(Map<String, Object> param);
*/

    /**
     * 学生数量统计
     */
    Long getStudentCount();

    /**
     * 教师数量统计
     */
    Long getTeacherCount();

    /**
     * 教学思政数量统计
     */
    Long getIdeologicalEducationCount();

    /**
     * 教学活动数量统计
     */
    Long getTeachingActivitiesCount();

    /**
     * 题库数量统计
     */
    Long getQuestionBankCount();

    /**
     * 班级数量统计
     */
    Long getClassCount();

    /**
     * 在授课程数量统计
     */
    Long getTeachingCourseCount();

    /**
     * 智慧教材数量统计
     */
    Long getSmartTextbookCount();

    /**
     * 教材类型统计
     */
    Map<String, Long> getTextbookTypeCount();

    /**
     * 教材阅读排名（按时长统计，支持时间范围查询）
     */
    List<Map<String, Object>> getTextbookReadingRank(Map<String, Object> params);

    /**
     * 类型阅读排名（按时长统计，支持时间范围查询）
     */
    List<Map<String, Object>> getTextbookTypeReadingRank(Map<String, Object> params);
    
    /**
     * 获取指定学生在某教材下各章节的掌握度
     * @param studentId 学生ID
     * @param textbookId 教材ID
     * @return List<ChapterMasteryVO> 章节掌握度列表
     */
    List<ChapterMasteryVO> getStudentChapterMastery(Long studentId, Long textbookId);

    /**
     * 交流反馈数量统计
     */
    Long getCommunicationFeedbackCount();

    /**
     * 教学素材数量统计
     */
    Long getTeachingMaterialsCount();

    /**
     * 资源使用数据统计
     */
    Map<String, Object> getResourceUsageStatistics();

}