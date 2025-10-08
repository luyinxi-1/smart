package com.upc.modular.datastatistics.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.datastatistics.controller.param.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SystemDataStatisticsMapper {

    // 今日访问人数
    Long getTodayVisitorCount();
//按时间段统计今日访问人数
    List<StatisticsDto> getTodayVisitorCountByPeriod();

List<VisitorCountDTO> getStudentVisitorCountByTime(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
);
    // 今日总学习时长
    Long getTodayStudyDuration();
    // 按时间段统计今日总学习时长
    List<StatisticsDto> getTodayStudyDurationByPeriod();

    /**
     * 按类型统计已发布的教材数量 (关联查询)
     * @return List<TextbookTypeCountDto>
     */
    List<TextbookTypeCountDto> countPublishedTextbookByType();

    /**
     * 根据时间范围和统计类型获取学习趋势数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param type 统计类型 ('day', 'week', 'month')
     * @return 学习趋势数据列表
     */
    List<StudyTrendDTO> getStudyTrendByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("type") String type
    );
    /**
     * 教材阅读排名（按时长统计，支持时间范围查询）
     * @param params 可能包含 startTime 和 endTime
     */
    List<Map<String, Object>> getTextbookReadingRank(Map<String, Object> params);

    /**
     * 类型阅读排名（按时长统计，支持时间范围查询）
     * @param params 可能包含 startTime 和 endTime
     */
    List<Map<String, Object>> getTextbookTypeReadingRank(Map<String, Object> params);


    /**
     * 获取指定学生在某教材下各章节的掌握度
     * @param studentId 学生ID
     * @param textbookId 教材ID
     * @return List<Map> 章节掌握度列表
     */
    List<Map<String, Object>> getStudentChapterMastery(@Param("studentId") Long studentId, @Param("textbookId") Long textbookId);

    /**
     * 获取教材更新申请记录
     */
    /**
     * 获取教材更新申请记录 (分页)
     */
// 修改点①: Mapper 方法的第一个参数必须是 Page 对象, 返回 IPage
    IPage<TextbookUpdateApplicationParam> getTextbookUpdateApplications(Page<TextbookUpdateApplicationParam> page);
}