package com.upc.modular.datastatistics.service;

import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.controller.param.TextbookDataStatisticsParam;
import com.upc.modular.datastatistics.controller.param.TextbookTimeStatisticsReturnParam;
import com.upc.modular.datastatistics.controller.param.TextbookTimeStatisticsSearchParam;

import java.util.List;

/**
 * 教师端教材数据统计服务接口
 */
public interface ITeacherTextbookStatisticsService {

    /**
     * 获取教材数据统计
     * @param textbookId 教材ID
     * @return 教材数据统计
     */
    TextbookDataStatisticsParam getTextbookDataStatistics(Long textbookId);

    /**
     * 按时间统计交流反馈新增数量
     * @param param 搜索参数
     * @return 时间统计结果
     */
    List<TextbookTimeStatisticsReturnParam> getCommunicationFeedbackStatisticsByTime(TextbookTimeStatisticsSearchParam param);

    /**
     * 按时间统计阅读时长
     * @param param 搜索参数
     * @return 时间统计结果
     */
    List<TextbookTimeStatisticsReturnParam> getReadingDurationStatisticsByTime(TextbookTimeStatisticsSearchParam param);

    /**
     * 获取各章节习题正确率统计
     * @param textbookId 教材ID
     * @return 各章节习题正确率统计
     */
    List<ChapterQuestionCorrectRateParam> getChapterQuestionCorrectRateStatistics(Long textbookId);

    /**
     * 获取教师所有教材信息
     * @param teacherId 教师ID
     * @return 教师教材列表
     */
    List<TeacherTextbookInfoParam> getTeacherTextbooks(Long teacherId);

    /**
     * 获取教师教材统计概览
     * @param teacherId 教师ID
     * @return 教材统计概览列表
     */
    List<TextbookStatisticsOverviewParam> getTeacherTextbookStatisticsOverview(Long teacherId);

    /**
     * 获取教材阅读人员统计
     * @param textbookId 教材ID
     * @return 阅读人员统计列表
     */
    List<ReaderStatisticsParam> getTextbookReaderStatistics(Long textbookId);

    /**
     * 获取教材做题情况统计
     * @param textbookId 教材ID
     * @return 做题情况统计列表
     */
    List<QuestionAnsweringStatisticsParam> getTextbookQuestionAnsweringStatistics(Long textbookId);

    /**
     * 获取学生做题情况统计
     * @param textbookId 教材ID
     * @param studentId 学生ID
     * @return 学生做题情况统计列表
     */
    List<StudentQuestionAnsweringStatisticsParam> getStudentQuestionAnsweringStatistics(Long textbookId, Long studentId);
} 