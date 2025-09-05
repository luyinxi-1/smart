package com.upc.modular.datastatistics.service;

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
} 