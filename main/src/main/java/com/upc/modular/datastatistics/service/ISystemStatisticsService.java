package com.upc.modular.datastatistics.service;

import com.upc.modular.datastatistics.controller.param.ChapterMasteryVO;
import com.upc.modular.datastatistics.controller.param.StudyTrendDTO;
import com.upc.modular.datastatistics.controller.param.VisitorCountDTO;
import com.upc.modular.datastatistics.controller.param.TextbookUpdateApplicationParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ISystemStatisticsService {

    /**
     * 获取今日访问人数
     */
    Long getTodayVisitorCount();

    /**
     * 根据时间范围标识符获取学生访客数量
     *
     * @param timeRange 时间范围标识符 (例如 "week", "month", "year")
     * @return 包含每天访客数量的 DTO 列表
     */
    List<VisitorCountDTO> getStudentVisitorCountByTime(String timeRange);
    /**
     * 获取今日总学习时长
     */
    Long getTodayStudyDuration();

    /**
     * 根据日期范围和统计类型获取学习趋势数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 统计类型 ('day', 'week', 'month')
     * @return 学习趋势数据列表
     */
    List<StudyTrendDTO> getStudyTrendByDateRange(LocalDate startDate, LocalDate endDate, String type);


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

    /**
     * 获取教材更新申请记录
     */
    List<TextbookUpdateApplicationParam> getTextbookUpdateApplications();

}