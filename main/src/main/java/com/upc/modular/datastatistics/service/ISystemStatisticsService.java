package com.upc.modular.datastatistics.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.datastatistics.controller.param.*;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ISystemStatisticsService {

    /**
     * 获取今日访问人数
     */
    Long getTodayVisitorCount();
    //按时间段获取今日访问人数统计
    List<StatisticsDto> getTodayVisitorCountByPeriod();
    /**
     * 根据时间范围标识符获取学生访客数量
     *
     * @param timeRange 时间范围标识符 (例如 "week", "month", "year")
     * @return 包含每天访客数量的 DTO 列表
     */
    List<VisitorCountDTO> getStudentVisitorCountByTime(String timeRange);

    /**
     * 根据时间范围获取每日学习时长统计
     * @param timeRange 时间范围 ("week", "month", "year")
     * @return 每日学习时长的列表
     */
    List<DailyStudyDurationDto> getStudyDurationByTime(String timeRange);
    /**
     * 获取今日总学习时长
     */
    Long getTodayStudyDuration();
    //按时间段获取今日学习时长统计
    List<StatisticsDto> getTodayStudyDurationByPeriod();

    /**
     * 根据日期范围和统计类型获取学习趋势数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 统计类型 ('day', 'week', 'month')
     * @return 学习趋势数据列表
     */
    List<StudyTrendDTO> getStudyTrendByDateRange(LocalDate startDate, LocalDate endDate, String type);


    SystemAllCountsDto getAllCounts(String dateStr);


    /**
     * 教材类型统计
     * @return List<TextbookTypeCountDto>
     */
    List<TextbookTypeCountDto> getTextbookTypeCount();
    /**
     * 教师数量统计
     */
    Long getTeacherCount();

    /**
     * 班级数量统计
     */
    Long getClassCount();

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

    IPage<TextbookUpdateApplicationParam> getTextbookUpdateApplications(Page<TextbookUpdateApplicationParam> page);

    /**
     * 获取全系统教材热度排名
     * @return List<TeacherTextbookPopularityParam>
     */
    List<TeacherTextbookPopularityParam> getSystemTextbookPopularity();

    /**
     * 获取全系统教材统计概览 (分页)
     * @param page 分页参数
     * @return IPage<TextbookStatisticsOverviewParam>
     */
    IPage<TextbookStatisticsOverviewParam> getSystemTextbookStatisticsOverview(Page<TextbookStatisticsOverviewParam> page);

    /**
     * 获取教材阅读人员统计 (分页)
     * @param page 分页参数
     * @param textbookId 教材ID
     * @return IPage<ReaderStatisticsParam>
     */
    IPage<ReaderStatisticsParam> getReaderStatistics(Page<ReaderStatisticsParam> page, @Param("textbookId") Long textbookId);

    /**
     * 按时间统计阅读时长
     * @param param 搜索参数
     * @return 时间统计结果
     */
    List<TextbookTimeStatisticsReturnParam> getReadingDurationStatisticsByTime(TextbookTimeStatisticsSearchParam param);

    /**
     * 按时间统计交流反馈新增数量
     * @param param 搜索参数
     * @return 时间统计结果
     */
    List<TextbookTimeStatisticsReturnParam> getCommunicationFeedbackStatisticsByTime(TextbookTimeStatisticsSearchParam param);

    /**
     * 获取各章节习题正确率统计
     * @param textbookId 教材ID
     * @return List<ChapterQuestionCorrectRateParam>
     */
    List<ChapterQuestionCorrectRateParam> getChapterQuestionCorrectRateStatistics(Long textbookId);

}
