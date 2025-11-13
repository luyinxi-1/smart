package com.upc.modular.datastatistics.service.impl;

import com.upc.modular.datastatistics.controller.param.*;
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
import java.time.LocalDate;
import java.time.DayOfWeek;
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
        
        // 处理章节名称，去除HTML标签和常见HTML实体字符，只返回纯文本名称
        for (ChapterQuestionCorrectRateParam param : result) {
            if (param.getChapterName() != null) {
                // 去除HTML标签
                String cleanName = param.getChapterName().replaceAll("<[^>]*>", "");
                // 只替换常见的HTML实体字符，避免误伤正常文本
                cleanName = cleanName.replace("&nbsp;", " ")
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&quot;", "\"")
                        .replace("&apos;", "'");
                // 将多个空格合并为单个空格
                cleanName = cleanName.replaceAll("\\s+", " ");
                // 去除首尾空格
                param.setChapterName(cleanName.trim());
            }
        }
        
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
        if (param.getTimeRange() != null && !param.getTimeRange().isEmpty()) {
            String[] timeRange = getTimeRangeByType(param.getTimeRange());
            if (timeRange != null) {
                records = teacherTextbookStatisticsMapper.findCommunicationFeedbackByTextbookIdAndTime(
                        param.getTextbookId(), timeRange[0], timeRange[1]);
            } else {
                records = teacherTextbookStatisticsMapper.findCommunicationFeedbackByTextbookId(param.getTextbookId());
            }
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
                // 根据时间范围类型格式化时间
                String timeKey = formatTimeByRangeType(reply.getAddDatetime(), param.getTimeRange());
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
        
        // 获取学习日志记录，只获取data_type=0的记录（有效阅读行为）
        List<LearningLog> records;
        if (param.getTimeRange() != null && !param.getTimeRange().isEmpty()) {
            String[] timeRange = getTimeRangeByType(param.getTimeRange());
            if (timeRange != null) {
                records = teacherTextbookStatisticsMapper.findLearningLogsByTextbookIdAndTime(
                        param.getTextbookId(), timeRange[0], timeRange[1]).stream()
                        .filter(log -> log.getDataType() == 0)
                        .collect(Collectors.toList());
            } else {
                records = teacherTextbookStatisticsMapper.findLearningLogsByTextbookId(param.getTextbookId()).stream()
                        .filter(log -> log.getDataType() == 0)
                        .collect(Collectors.toList());
            }
        } else {
            records = teacherTextbookStatisticsMapper.findLearningLogsByTextbookId(param.getTextbookId()).stream()
                    .filter(log -> log.getDataType() == 0)
                    .collect(Collectors.toList());
        }
        
        if (records == null || records.size() < 2) {
            return new ArrayList<>();
        }
        
        // 按用户分组，过滤掉creator为null的记录
        Map<Long, List<LearningLog>> userRecordsMap = records.stream()
                .filter(log -> log.getCreator() != null)  // 过滤掉creator为null的记录
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
                    // 根据时间范围类型格式化时间
                    String timeKey = formatTimeByRangeType(currentAddDatetime, param.getTimeRange());
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
        
        // 按时间排序，使用专门的比较器处理不同格式的时间字符串
        result.sort((o1, o2) -> {
            String time1 = o1.getTime();
            String time2 = o2.getTime();
            
            // 统一格式化为可比较的形式
            String formattedTime1 = formatTimeStringForSorting(time1);
            String formattedTime2 = formatTimeStringForSorting(time2);
            
            return formattedTime1.compareTo(formattedTime2);
        });
        
        return result;
    }

    /**
     * 根据时间范围类型获取开始和结束时间
     * @param timeRange 时间范围类型
     * @return 包含开始时间和结束时间的数组 [startTime, endTime]
     */
    private String[] getTimeRangeByType(String timeRange) {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        switch (timeRange.toLowerCase()) {
            case "week":
                // 本周：从本周一开始到今天
                LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
                return new String[]{startOfWeek.format(formatter), now.format(formatter)};
            case "month":
                // 本月：从本月1号到今天
                LocalDate startOfMonth = now.withDayOfMonth(1);
                return new String[]{startOfMonth.format(formatter), now.format(formatter)};
            case "year":
                // 本年：从今年1月1号到今天
                LocalDate startOfYear = now.withDayOfYear(1);
                return new String[]{startOfYear.format(formatter), now.format(formatter)};
            default:
                // 默认返回null，表示不限制时间范围
                return null;
        }
    }

    /**
     * 格式化时间字符串用于排序
     * @param timeString 时间字符串
     * @return 格式化后的时间字符串
     */
    private String formatTimeStringForSorting(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return timeString;
        }
        
        // 如果是年月格式(yyyy-MM)，补充为日期格式(yyyy-MM-dd)
        if (timeString.length() == 7 && timeString.charAt(4) == '-') {
            return timeString + "-01";
        }
        
        return timeString;
    }

    /**
     * 根据时间范围类型格式化时间
     * @param dateTime 时间
     * @param timeRange 时间范围类型（week/month/year）
     * @return 格式化后的时间字符串
     */
    private String formatTimeByRangeType(LocalDateTime dateTime, String timeRange) {
        if (timeRange == null || timeRange.isEmpty()) {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        
        switch (timeRange.toLowerCase()) {
            case "week":
            case "month":
                // 按日显示
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "year":
                // 按月显示
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            default:
                // 默认按日显示
                return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    @Override
    public List<TeacherTextbookInfoParam> getTeacherTextbooks(Long teacherId) {
        List<Map<String, Object>> rawData = teacherTextbookStatisticsMapper.getTeacherTextbooks(teacherId);
        
        List<TeacherTextbookInfoParam> result = new ArrayList<>();
        
        for (Map<String, Object> data : rawData) {
            TeacherTextbookInfoParam param = new TeacherTextbookInfoParam();
            
            // 安全地处理可能为null的数值字段
            param.setTextbookId(getLongValue(data.get("textbookId")));
            param.setTextbookName((String) data.get("textbookName"));
            param.setTextbookType(getLongValue(data.get("textbookType")));
            param.setTextbookPublishingHouse((String) data.get("textbookPublishingHouse"));
            param.setTextbookVersion((String) data.get("textbookVersion"));
            param.setReleaseStatus(getIntValue(data.get("releaseStatus")));
            param.setCourseName((String) data.get("courseName"));
            param.setClassName((String) data.get("className"));
            
            result.add(param);
        }
        
        return result;
    }

    @Override
    public List<StudentQuestionAnsweringStatisticsParam> getStudentQuestionAnsweringStatistics(Long textbookId, Long studentId) {
        List<Map<String, Object>> rawData = teacherTextbookStatisticsMapper.getStudentQuestionAnsweringStatistics(textbookId, studentId);
        
        List<StudentQuestionAnsweringStatisticsParam> result = new ArrayList<>();
        
        for (Map<String, Object> data : rawData) {
            StudentQuestionAnsweringStatisticsParam param = new StudentQuestionAnsweringStatisticsParam();
            
            param.setChapterId(getLongValue(data.get("chapterId")));
            // 清理章节名称中的HTML标签
            String chapterName = (String) data.get("chapterName");
            if (chapterName != null) {
                // 去除HTML标签
                chapterName = chapterName.replaceAll("<[^>]*>", "");
                // 只替换常见的HTML实体字符，避免误伤正常文本
                chapterName = chapterName.replace("&nbsp;", " ")
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&quot;", "\"")
                        .replace("&apos;", "'");
                // 将多个空格合并为单个空格
                chapterName = chapterName.replaceAll("\\s+", " ");
                // 去除首尾空格
                chapterName = chapterName.trim();
            }
            param.setChapterName(chapterName);
            param.setQuestionAnsweringDuration(getLongValue(data.get("questionAnsweringDuration")));
            param.setChapterScore(getDoubleValue(data.get("chapterScore")));
            param.setCorrectRate(getDoubleValue(data.get("correctRate")));
            param.setTotalQuestions(getIntValue(data.get("totalQuestions")));
            param.setCorrectAnswers(getIntValue(data.get("correctAnswers")));
            param.setAverageScore(getDoubleValue(data.get("averageScore")));
            param.setChapterLevel(getIntValue(data.get("chapterLevel")));
            param.setParentChapterId(getLongValue(data.get("parentChapterId")));
            param.setReadingDuration(getLongValue(data.get("readingDuration")));
            param.setMasteryPercentage(getDoubleValue(data.get("masteryPercentage")));
            
            result.add(param);
        }
        
        return result;
    }

    /**
     * 安全地获取Long值
     * @param obj 对象
     * @return Long值，如果为null则返回null
     */
    private Long getLongValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return null;
    }

    /**
     * 安全地获取Integer值
     * @param obj 对象
     * @return Integer值，如果为null则返回null
     */
    private Integer getIntValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return null;
    }

    @Override
    public List<TextbookStatisticsOverviewParam> getTeacherTextbookStatisticsOverview(Long teacherId) {
        List<Map<String, Object>> rawData = teacherTextbookStatisticsMapper.getTeacherTextbookStatisticsOverview(teacherId);
        
        List<TextbookStatisticsOverviewParam> result = new ArrayList<>();
        
        for (Map<String, Object> data : rawData) {
            TextbookStatisticsOverviewParam param = new TextbookStatisticsOverviewParam();
            
            param.setTextbookId(getLongValue(data.get("textbookId")));
            param.setTextbookName((String) data.get("textbookName"));
            param.setReaderCount(getLongValue(data.get("readerCount")));
            param.setTeachingActivityCount(getLongValue(data.get("teachingActivityCount")));
            param.setMaterialCount(getLongValue(data.get("materialCount")));
            param.setCommunicationFeedbackCount(getLongValue(data.get("communicationFeedbackCount")));
            param.setIdeologicalMaterialCount(getLongValue(data.get("ideologicalMaterialCount")));
            param.setQuestionCorrectRate(getDoubleValue(data.get("questionCorrectRate")));
            param.setCommunicationParticipationCount(getLongValue(data.get("communicationParticipationCount")));
            param.setAnnotationCount(getLongValue(data.get("annotationCount")));
            
            result.add(param);
        }
        
        return result;
    }

    @Override
    public List<ReaderStatisticsParam> getTextbookReaderStatistics(Long textbookId) {
        List<Map<String, Object>> rawData = teacherTextbookStatisticsMapper.getTextbookReaderStatistics(textbookId);
        
        List<ReaderStatisticsParam> result = new ArrayList<>();
        
        for (Map<String, Object> data : rawData) {
            ReaderStatisticsParam param = new ReaderStatisticsParam();
            
            param.setStudentId(getLongValue(data.get("studentId")));
            param.setStudentName((String) data.get("studentName"));
            param.setReadingDuration(getLongValue(data.get("readingDuration")));
            param.setLearningBehavior((String) data.get("learningBehavior"));
            param.setChapterCount(getIntValue(data.get("chapterCount")));
            param.setLastReadingTime(getStringValue(data.get("lastReadingTime")));
            param.setProgressPercentage(getDoubleValue(data.get("progressPercentage")));
            
            result.add(param);
        }
        
        return result;
    }

    @Override
    public List<QuestionAnsweringStatisticsParam> getTextbookQuestionAnsweringStatistics(Long textbookId) {
        List<Map<String, Object>> rawData = teacherTextbookStatisticsMapper.getTextbookQuestionAnsweringStatistics(textbookId);
        
        List<QuestionAnsweringStatisticsParam> result = new ArrayList<>();
        
        for (Map<String, Object> data : rawData) {
            QuestionAnsweringStatisticsParam param = new QuestionAnsweringStatisticsParam();
            
            param.setChapterId(getLongValue(data.get("chapterId")));
            param.setChapterName((String) data.get("chapterName"));
            param.setMasteryLevel(getDoubleValue(data.get("masteryLevel")));
            param.setTotalQuestions(getIntValue(data.get("totalQuestions")));
            param.setCorrectAnswers(getIntValue(data.get("correctAnswers")));
            param.setCorrectRate(getDoubleValue(data.get("correctRate")));
            param.setParticipantCount(getIntValue(data.get("participantCount")));
            param.setAverageScore(getDoubleValue(data.get("averageScore")));
            param.setChapterLevel(getIntValue(data.get("chapterLevel")));
            param.setParentChapterId(getLongValue(data.get("parentChapterId")));
            
            result.add(param);
        }
        
        return result;
    }

    /**
     * 安全地获取Double值
     * @param obj 对象
     * @return Double值，如果为null则返回null
     */
    private Double getDoubleValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return null;
    }

    /**
     * 安全地获取String值
     * @param obj 对象
     * @return String值，如果为null则返回null
     */
    private String getStringValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof java.sql.Timestamp) {
            return obj.toString();
        }
        if (obj instanceof java.util.Date) {
            return obj.toString();
        }
        return obj.toString();
    }
} 