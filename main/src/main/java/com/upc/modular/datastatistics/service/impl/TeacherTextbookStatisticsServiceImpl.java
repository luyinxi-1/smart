package com.upc.modular.datastatistics.service.impl;

import com.upc.modular.datastatistics.controller.param.ChapterQuestionCorrectRateParam;
import com.upc.modular.datastatistics.controller.param.TextbookDataStatisticsParam;
import com.upc.modular.datastatistics.controller.param.TextbookTimeStatisticsReturnParam;
import com.upc.modular.datastatistics.controller.param.TextbookTimeStatisticsSearchParam;
import com.upc.modular.datastatistics.mapper.TeacherTextbookStatisticsMapper;
import com.upc.modular.datastatistics.service.ITeacherTextbookStatisticsService;
import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import com.upc.modular.textbook.entity.LearningLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师端教材数据统计服务实现
 */
@Slf4j
@Service
public class TeacherTextbookStatisticsServiceImpl implements ITeacherTextbookStatisticsService {

    @Autowired
    private TeacherTextbookStatisticsMapper teacherTextbookStatisticsMapper;

    @Override
    public TextbookDataStatisticsParam getTextbookDataStatistics(Long textbookId) {
        log.info("获取教材数据统计，教材ID: {}", textbookId);
        
        TextbookDataStatisticsParam param = new TextbookDataStatisticsParam();
        
        // 统计阅读人数
        Long readerCount = teacherTextbookStatisticsMapper.countReadersByTextbookId(textbookId);
        param.setReaderCount(readerCount);
        
        // 统计教学活动数量
        Long teachingActivityCount = teacherTextbookStatisticsMapper.countTeachingActivitiesByTextbookId(textbookId);
        param.setTeachingActivityCount(teachingActivityCount);
        
        // 统计素材数量
        Long materialCount = teacherTextbookStatisticsMapper.countMaterialsByTextbookId(textbookId);
        param.setMaterialCount(materialCount);
        
        // 统计阅读时长 - 使用新的基于时间间隔的算法
        Long readingDurationMinutes = calculateTextbookReadingDuration(textbookId);
        param.setReadingDurationMinutes(readingDurationMinutes);
        
        // 统计交流反馈数量
        Long communicationFeedbackCount = teacherTextbookStatisticsMapper.countCommunicationFeedbackByTextbookId(textbookId);
        param.setCommunicationFeedbackCount(communicationFeedbackCount);
        
        // 统计教学思政数量
        Long ideologicalMaterialCount = teacherTextbookStatisticsMapper.countIdeologicalMaterialsByTextbookId(textbookId);
        param.setIdeologicalMaterialCount(ideologicalMaterialCount);

        // 统计答题正确率
        Double questionCorrectRate = teacherTextbookStatisticsMapper.getQuestionCorrectRateByTextbookId(textbookId);
        param.setQuestionCorrectRate(questionCorrectRate != null ? questionCorrectRate : 0.0);
        
        log.info("教材数据统计完成，教材ID: {}, 统计结果: {}", textbookId, param);
        return param;
    }

    @Override
    public List<TextbookTimeStatisticsReturnParam> getCommunicationFeedbackStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        log.info("按时间统计交流反馈新增数量，参数: {}", param);

        // 使用新的基于时间格式化的算法
        List<TextbookTimeStatisticsReturnParam> result = calculateCommunicationFeedbackStatisticsByTime(param);

        log.info("按时间统计交流反馈新增数量完成，结果数量: {}", result.size());
        return result;
    }

    @Override
    public List<TextbookTimeStatisticsReturnParam> getReadingDurationStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        log.info("按时间统计阅读时长，参数: {}", param);
        
        // 使用新的基于时间间隔的算法
        List<TextbookTimeStatisticsReturnParam> result = calculateReadingDurationStatisticsByTime(param);
        
        log.info("按时间统计阅读时长完成，结果数量: {}", result.size());
        return result;
    }

    @Override
    public List<ChapterQuestionCorrectRateParam> getChapterQuestionCorrectRateStatistics(Long textbookId) {
        log.info("获取各章节习题正确率统计，教材ID: {}", textbookId);
        
        List<ChapterQuestionCorrectRateParam> result = teacherTextbookStatisticsMapper
                .getChapterQuestionCorrectRateStatistics(textbookId);
        
        log.info("获取各章节习题正确率统计完成，结果数量: {}", result.size());
        return result;
    }

    /**
     * 按时间统计交流反馈数量 - 使用改进的时间格式化算法
     * @param param 搜索参数
     * @return 时间统计结果
     */
    private List<TextbookTimeStatisticsReturnParam> calculateCommunicationFeedbackStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        // 获取交流反馈记录
        List<DiscussionTopicReply> records;
        if (param.getStartTime() != null && param.getEndTime() != null) {
            // 转换时间格式
            String formattedStartTime = formatTimeForDatabase(param.getStartTime(), param.getQueryMethod(), true);
            String formattedEndTime = formatTimeForDatabase(param.getEndTime(), param.getQueryMethod(), false);

            records = teacherTextbookStatisticsMapper.findCommunicationFeedbackByTextbookIdAndTime(
                    param.getTextbookId(), formattedStartTime, formattedEndTime);
        } else {
            records = teacherTextbookStatisticsMapper.findCommunicationFeedbackByTextbookId(param.getTextbookId());
        }

        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        // 按时间维度统计
        Map<String, Long> timeFeedbackMap = new HashMap<>();

        for (DiscussionTopicReply reply : records) {
            if (reply.getAddDatetime() != null) {
                // 根据查询方式格式化时间
                String timeKey = formatTimeByQueryMethod(reply.getAddDatetime(), param.getQueryMethod());
                timeFeedbackMap.put(timeKey, timeFeedbackMap.getOrDefault(timeKey, 0L) + 1);
            }
        }

        // 转换为返回结果
        List<TextbookTimeStatisticsReturnParam> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : timeFeedbackMap.entrySet()) {
            TextbookTimeStatisticsReturnParam returnParam = new TextbookTimeStatisticsReturnParam();
            returnParam.setTime(entry.getKey());
            returnParam.setCount(entry.getValue()); // 交流反馈数量
            returnParam.setDuration(entry.getValue()); // 同时设置duration字段，保持兼容性
            result.add(returnParam);
        }

        // 按时间排序
        result.sort(Comparator.comparing(TextbookTimeStatisticsReturnParam::getTime));

        return result;
    }

    /**
     * 计算指定教材的总阅读时长
     * @param textbookId 教材ID
     * @return 总阅读时长（分钟）
     */
    private Long calculateTextbookReadingDuration(Long textbookId) {
        // 容忍范围
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;
        
        // 获取学习日志记录
        List<LearningLog> records = teacherTextbookStatisticsMapper.findLearningLogsByTextbookId(textbookId);
        
        if (records == null || records.size() < 2) {
            return 0L;
        }
        
        // 按用户分组统计阅读时间
        Map<Long, List<LearningLog>> userRecordsMap = records.stream()
                .collect(Collectors.groupingBy(LearningLog::getCreator));
        
        long totalReadingTime = 0L;
        
        for (Map.Entry<Long, List<LearningLog>> entry : userRecordsMap.entrySet()) {
            List<LearningLog> userRecords = entry.getValue();
            
            // 按时间排序
            userRecords.sort(Comparator.comparing(LearningLog::getAddDatetime));
            
            for (int i = 0; i < userRecords.size() - 1; i++) {
                LocalDateTime currentAddDatetime = userRecords.get(i).getAddDatetime();
                LocalDateTime nextAddDatetime = userRecords.get(i + 1).getAddDatetime();
                
                if (currentAddDatetime == null || nextAddDatetime == null) {
                    continue;
                }
                
                Duration duration = Duration.between(currentAddDatetime, nextAddDatetime);
                long seconds = duration.getSeconds();
                
                if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                    totalReadingTime += 1; // 每个有效间隔计为1分钟
                }
            }
        }
        
        return totalReadingTime;
    }

    /**
     * 按时间统计阅读时长
     * @param param 搜索参数
     * @return 时间统计结果
     */
    private List<TextbookTimeStatisticsReturnParam> calculateReadingDurationStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        // 容忍范围
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;
        
        // 获取学习日志记录
        List<LearningLog> records;
        if (param.getStartTime() != null && param.getEndTime() != null) {
            // 转换时间格式
            String formattedStartTime = formatTimeForDatabase(param.getStartTime(), param.getQueryMethod(), true);
            String formattedEndTime = formatTimeForDatabase(param.getEndTime(), param.getQueryMethod(), false);
            
            records = teacherTextbookStatisticsMapper.findLearningLogsByTextbookIdAndTime(
                    param.getTextbookId(), formattedStartTime, formattedEndTime);
        } else {
            records = teacherTextbookStatisticsMapper.findLearningLogsByTextbookId(param.getTextbookId());
        }
        
        if (records == null || records.size() < 2) {
            return new ArrayList<>();
        }
        
        // 按用户分组
        Map<Long, List<LearningLog>> userRecordsMap = records.stream()
                .collect(Collectors.groupingBy(LearningLog::getCreator));
        
        // 按时间维度统计
        Map<String, Long> timeReadingMap = new HashMap<>();
        
        for (Map.Entry<Long, List<LearningLog>> entry : userRecordsMap.entrySet()) {
            List<LearningLog> userRecords = entry.getValue();
            
            // 按时间排序
            userRecords.sort(Comparator.comparing(LearningLog::getAddDatetime));
            
            for (int i = 0; i < userRecords.size() - 1; i++) {
                LocalDateTime currentAddDatetime = userRecords.get(i).getAddDatetime();
                LocalDateTime nextAddDatetime = userRecords.get(i + 1).getAddDatetime();
                
                if (currentAddDatetime == null || nextAddDatetime == null) {
                    continue;
                }
                
                Duration duration = Duration.between(currentAddDatetime, nextAddDatetime);
                long seconds = duration.getSeconds();
                
                if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                    // 根据查询方式格式化时间
                    String timeKey = formatTimeByQueryMethod(currentAddDatetime, param.getQueryMethod());
                    timeReadingMap.put(timeKey, timeReadingMap.getOrDefault(timeKey, 0L) + 1);
                }
            }
        }
        
        // 转换为返回结果
        List<TextbookTimeStatisticsReturnParam> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : timeReadingMap.entrySet()) {
            TextbookTimeStatisticsReturnParam returnParam = new TextbookTimeStatisticsReturnParam();
            returnParam.setTime(entry.getKey());
            returnParam.setDuration(entry.getValue()); // 阅读时长（分钟）
            returnParam.setCount(entry.getValue()); // 同时设置count字段，保持兼容性
            result.add(returnParam);
        }
        
        // 按时间排序
        result.sort(Comparator.comparing(TextbookTimeStatisticsReturnParam::getTime));
        
        return result;
    }

    /**
     * 将前端传入的时间格式转换为数据库可识别的格式
     * @param timeStr 前端传入的时间字符串
     * @param queryMethod 查询方式（1-按日，2-按月，3-按年）
     * @param isStartTime 是否为开始时间
     * @return 格式化后的时间字符串
     */
    private String formatTimeForDatabase(String timeStr, Integer queryMethod, boolean isStartTime) {
        if (queryMethod == null) {
            queryMethod = 1; // 默认按日
        }
        
        switch (queryMethod) {
            case 1: // 按日 - 格式：2024-01-15
                return timeStr; // 已经是完整日期格式
            case 2: // 按月 - 格式：2024-01
                if (isStartTime) {
                    return timeStr + "-01"; // 月初：2024-01-01
                } else {
                    return timeStr + "-31"; // 月末：2024-01-31
                }
            case 3: // 按年 - 格式：2024
                if (isStartTime) {
                    return timeStr + "-01-01"; // 年初：2024-01-01
                } else {
                    return timeStr + "-12-31"; // 年末：2024-12-31
                }
            default:
                return timeStr;
        }
    }

    /**
     * 根据查询方式格式化时间
     * @param dateTime 时间
     * @param queryMethod 查询方式（1-按日，2-按月，3-按年）
     * @return 格式化后的时间字符串
     */
    private String formatTimeByQueryMethod(LocalDateTime dateTime, Integer queryMethod) {
        if (queryMethod == null) {
            queryMethod = 1; // 默认按日
        }

        switch (queryMethod) {
            case 1: // 按日
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case 2: // 按月
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case 3: // 按年
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy"));
            default:
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
} 