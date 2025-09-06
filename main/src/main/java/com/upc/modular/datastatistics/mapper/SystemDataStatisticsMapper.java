package com.upc.modular.datastatistics.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SystemDataStatisticsMapper {

    // 教材阅读排名
    List<Map<String, Object>> getTextbookReadingRank();

    // 类型阅读排名
    List<Map<String, Object>> getTextbookTypeReadingRank();

    // 今日访问人数
    Long getTodayVisitorCount();

    // 按时间统计访问人数
    List<Map<String, Object>> getVisitorCountByTime(Map<String, Object> param);

    // 今日总学习时长
    Long getTodayStudyDuration();

    // 按时间统计总学习时长
    List<Map<String, Object>> getStudyDurationByTime(Map<String, Object> param);

    // 今日活跃人数
    Long getTodayActiveUserCount();

    // 按时间统计活跃人数
    List<Map<String, Object>> getActiveUserCountByTime(Map<String, Object> param);

    // 学生数量统计
    Long getStudentCount();

    // 教学思政数量统计
    Long getIdeologicalMaterialCount();

    // 教学活动数量统计
    Long getTeachingActivityCount();

    // 题库数量统计
    Long getQuestionBankCount();

    // 班级数量统计
    Long getClassCount();

    // 在授课程数量统计
    Long getTeachingCourseCount();

    // 智慧教材数量统计
    Long getSmartTextbookCount();

    // 教材类型统计
    List<Map<String, Object>> getTextbookTypeCount();

    // 教师数量统计
    Long getTeacherCount();

    // 交流反馈数量统计
    Long getCommunicationFeedbackCount();

    // 教学素材数量统计
    Long getTeachingMaterialCount();

    // 资源使用数据统计
    List<Map<String, Object>> getResourceUsageData();
}
