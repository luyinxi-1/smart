package com.upc.modular.datastatistics.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.entity.StudentStatisticsData;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IStudentDataStatistics extends IService<StudentStatisticsData> {
    Long countStudentTextbookReading();

    Long countStudentFavoritebook();

    Long countStudentTeachingActivities();

    Long countStudentCommunicationFeedback();

    Long countStudentnotes();

    Long countStudentQuestions();

    Long countStudentTextbookReadingTime();

    List<StudentReadingTimeByMonthReturnParam> countStudentTextbookReadingTimeByMonth(Integer year);

    List<StudentTextbookCompletionReturnParam> countStudentTextbookCompetion();

    List<StudentTextbookCompletionReturnParam> countStudentTextbookCompetion(String start_time,String end_time);

    Long countStudentTextbookRead();

    Long countStudentTextbookRead(String startTime, String endTime);

    List<StudentStatisticsData> countStudentCurrentYearTextbookReadingTime();

    List<StudentStatisticsData> countStudentCurrentTextbookRead();

    Long countStudentTextbookReadingTimeByTime(String startTime, String endTime);

    StudentStudyPathReturnParam countStudentStudyPath();
    
    /**
     * 统计学生按教材和章节的阅读时长
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每个章节的阅读时长映射
     */
    Map<String, Long> countStudentTextbookReadingTimeByChapter(String startTime, String endTime);

    StudentBehaviorReturnParam analyzeStudentBehavior(Long targetUserId,String startTime, String endTime);

    StudentAnalysisReturnParam countStudentPersonalAnalysis(String startTime, String endTime);

    StudentTextbookSituationReturnParam countStudentTextbookSituation(Long textbookId);

    List<StudentTextbookRankParam> countStudentTextbookReadingRank();

    /**
     * 根据班级名称和学生姓名分页查询学生阅读排名
     * @param groupName 班级名称
     * @param studentName 学生姓名
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<StudentReadingRankParam> getStudentReadingRankByPage(String groupName, String studentName, Long current, Long size);
    void exportStudentReadingRank(String groupName, String studentName, HttpServletResponse response);

    List<StudentTextbookRankParam> countStudentTextbookReadingRankByStudentId(Long studentId);

    /**
     * 根据学生ID导出阅读过的教材排名（Excel）
     */
    void exportStudentTextbookReadingRankByStudentId(Long studentId, HttpServletResponse response);
    
    /**
     * 获取学生的平均得分率
     * @param studentId 学生ID
     * @return 平均得分率（百分比形式，保留两位小数）
     */
    Double getStudentScoreRate(Long studentId);

    List<TextStudentRankParam> countStudentTextbookReadingRankByTextbookId(Long textbookId);
}