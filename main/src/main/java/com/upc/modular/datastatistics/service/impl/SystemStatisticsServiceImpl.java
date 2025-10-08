package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.course.service.ICourseService;
import com.upc.modular.datastatistics.mapper.SystemDataStatisticsMapper;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import com.upc.modular.group.service.IGroupService;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.questionbank.service.ITeachingQuestionBankService;
import com.upc.modular.questionbank.service.ITeachingQuestionService;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.upc.modular.teachingactivities.service.IDiscussionTopicReplyService;
import com.upc.modular.teachingactivities.service.IDiscussionTopicService;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.service.IIdeologicalMaterialService;
import com.upc.modular.textbook.service.ITextbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class SystemStatisticsServiceImpl implements ISystemStatisticsService {
    private static final Set<String> ALLOWED_TYPES = Collections.unmodifiableSet(new HashSet<String>() {{
        add("day");
        add("week");
        add("month");
    }});

    @Autowired
    private TextbookMapper textbookMapper;

    @Autowired
    private ITeachingMaterialsService teachingMaterialsService;

    @Autowired
    private TeachingMaterialsMapper teachingMaterialsMapper;
    @Autowired
    private ITeacherService teacherService;
    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private ICourseService courseService;
    @Autowired
    private IGroupService groupService;
    @Autowired
    private ITeachingQuestionService teachingQuestionService;
    @Autowired
    private ITeachingQuestionBankService teachingQuestionbankService;
    @Autowired
    private IDiscussionTopicService discussionTopicService;
    @Autowired
    private IIdeologicalMaterialService ideologicalMaterialService;
    @Autowired
    private IStudentService studentService;
    @Autowired
    private IDiscussionTopicReplyService discussionTopicReplyService;
    @Autowired
    private SystemDataStatisticsMapper systemDataStatisticsMapper;

    // 添加SysLogMapper的依赖
    @Autowired
    private com.upc.modular.auth.mapper.SysLogMapper userLoginLogMapper;

    //今日访问人数
    @Override
    public Long getTodayVisitorCount() {

        return systemDataStatisticsMapper.getTodayVisitorCount();
    }

    @Override
    public List<StatisticsDto> getTodayVisitorCountByPeriod() {
        // 从数据库获取按时间段分组的原始数据
        List<StatisticsDto> resultsFromDb = systemDataStatisticsMapper.getTodayVisitorCountByPeriod();

        // 创建一个从时间段到数值的映射，方便查找
        Map<String, Long> resultMap = resultsFromDb.stream()
                .collect(Collectors.toMap(StatisticsDto::getTimeSlot, StatisticsDto::getValue));

        // 定义所有期望的时间段，确保返回结果的完整性
        List<String> timeSlots = Arrays.asList(
                "0:00-4:00", "4:00-8:00", "8:00-12:00",
                "12:00-16:00", "16:00-20:00", "20:00-24:00"
        );

        // 遍历所有时间段，如果数据库中没有该时间段的数据，则补充为0
        return timeSlots.stream()
                .map(slot -> new StatisticsDto(slot, resultMap.getOrDefault(slot, 0L)))
                .collect(Collectors.toList());
    }
    //按时间统计访问人数
    @Override
    public List<VisitorCountDTO> getStudentVisitorCountByTime(String timeRange) {
        // 这部分日期计算逻辑不变
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        switch (timeRange.toLowerCase()) {
            case "month":
                startDate = endDate.minusMonths(1).plusDays(1);
                break;
            case "year":
                startDate = endDate.minusYears(1).plusDays(1);
                break;
            default:
                startDate = endDate.minusDays(6);
                break;
        }

        // 从数据库获取数据
        List<VisitorCountDTO> dbResults = systemDataStatisticsMapper.getStudentVisitorCountByTime(startDate, endDate);

        // 将包含 java.util.Date 的列表转换为以 LocalDate 为键的 Map
        Map<LocalDate, VisitorCountDTO> resultsMap = dbResults.stream()
                .collect(Collectors.toMap(
                        // 关键：将 java.util.Date 转换为 LocalDate
                        dto -> dto.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        dto -> dto,
                        (existing, replacement) -> existing // 用于处理重复的键，虽然这里不太可能发生
                ));

        // 生成完整日期范围并补全数据
        long numOfDays = startDate.until(endDate, ChronoUnit.DAYS) + 1;

        return Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(numOfDays)
                .map(date -> {
                    VisitorCountDTO foundDto = resultsMap.get(date);
                    if (foundDto != null) {
                        return foundDto;
                    } else {
                        // 关键：创建 DTO 时，将 LocalDate 转换回 java.util.Date
                        Date missingDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        return new VisitorCountDTO(missingDate, 0L);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long getTodayStudyDuration() {
        // TODO: 实现今日总学习时长统计逻辑
        return systemDataStatisticsMapper.getTodayStudyDuration();
    }

    @Override
    public List<StatisticsDto> getTodayStudyDurationByPeriod() {
        // 从数据库获取按时间段分组的原始数据
        List<StatisticsDto> resultsFromDb = systemDataStatisticsMapper.getTodayStudyDurationByPeriod();

        // 创建映射
        Map<String, Long> resultMap = resultsFromDb.stream()
                .collect(Collectors.toMap(StatisticsDto::getTimeSlot, StatisticsDto::getValue));

        // 定义所有时间段
        List<String> timeSlots = Arrays.asList(
                "0:00-4:00", "4:00-8:00", "8:00-12:00",
                "12:00-16:00", "16:00-20:00", "20:00-24:00"
        );

        // 遍历并补全数据
        return timeSlots.stream()
                .map(slot -> new StatisticsDto(slot, resultMap.getOrDefault(slot, 0L)))
                .collect(Collectors.toList());
    }

    //按时间统计总学习时长
    @Override
    public List<StudyTrendDTO> getStudyTrendByDateRange(LocalDate startDate, LocalDate endDate, String type) {
        // 安全校验
        String lowerCaseType = type.toLowerCase();
        if (!ALLOWED_TYPES.contains(lowerCaseType)) {
            throw new IllegalArgumentException("无效的统计类型: " + type + ". 只允许 'day', 'week', 'month'。");
        }
        // 日期顺序的校验
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期。");
        }
        // 将日期转换为一整天的时间范围
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

        // 调用 Mapper，传入转换后的 LocalDateTime
        return systemDataStatisticsMapper.getStudyTrendByTimeRange(startTime, endTime, lowerCaseType);
    }

    @Override
    public Map<String, Long> getAllCounts() {
        Map<String, Long> countsMap = new LinkedHashMap<>();
        countsMap.put("TeacherCount", teacherService.count());//教师数量
        countsMap.put("StudentCount", studentService.count());  //学生数量
        countsMap.put("GroupCount", groupService.count());//班级数量
        countsMap.put("TeachingideologicalMaterialCount", ideologicalMaterialService.count());//教学思政数量

        // 修改 DiscussionTopicCount，只统计 identity_type 为 1 的教学活动数量
        Long discussionTopicCount = discussionTopicService.lambdaQuery()
                .eq(DiscussionTopic::getIdentityType, 1)
                .count();
        countsMap.put("DiscussionTopicCount", discussionTopicCount); //教学活动数量
        countsMap.put("DiscussionTopicReplyCount", discussionTopicReplyService.count());//交流反馈数量

        countsMap.put("TeachingQuestionBankCount", teachingQuestionbankService.count());//题库数量
        countsMap.put("CourseCount", courseService.count()); //在授课程数量
        countsMap.put("TeachingMaterialsCount", teachingMaterialsService.count());  //教学素材数量

        // 修改 TextbookCount，只统计 release_status 为 '1' 的教材数量
        Long textbookCount = textbookService.lambdaQuery()
                .eq(Textbook::getReleaseStatus, "1")
                .count();
        countsMap.put("TextbookCount", textbookCount);//智慧教材数量
        countsMap.put("TodayStudyTime", systemDataStatisticsMapper.getTodayStudyDuration());//今日总学习时长
       countsMap.put("TodayVisitorCount", systemDataStatisticsMapper.getTodayVisitorCount());//今日访问人数
        return countsMap;
    }
    @Override
    public Long getTeacherCount() {
        // TODO: 实现教师数量统计逻辑
        return teacherService.count();
    }

    @Override
    public Long getClassCount() {
        // TODO: 实现班级数量统计逻辑
        return groupService.count();
    }
    @Override
    public Long getCommunicationFeedbackCount() {
        // TODO: 实现交流反馈数量统计逻辑
        return discussionTopicReplyService.count();
    }

    @Override
    public Long getTeachingMaterialsCount() {
        // TODO: 实现教学素材数量统计逻辑
        return teachingMaterialsService.count();
    }
    //教材类型统计
    @Override
    public List<TextbookTypeCountDto> getTextbookTypeCount() {
        // 【关键】调用新 Mapper 中的方法
        return systemDataStatisticsMapper.countPublishedTextbookByType();
    }

    /**
     * 处理时间参数，确保符合业务需求：
     * 1. 如果startTime只提供了日期，默认设置为当天00:00:00
     * 2. 如果endTime只提供了日期，默认设置为当天23:59:59
     * 3. 如果提供了完整时间，则使用用户提供的具体时间
     * 4. endTime必须大于等于startTime
     * 5. 如果endTime晚于今天，就默认为今天
     * 6. 如果不输入，就默认为所有时间
     *
     * @param params 参数Map
     */
    private void processTimeParams(Map<String, Object> params) {
        if (params == null) {
            return;
        }

        String startTimeStr = null;
        String endTimeStr = null;

        // 获取原始参数
        Object startTimeObj = params.get("startTime");
        Object endTimeObj = params.get("endTime");

        // 处理startTime参数
        if (startTimeObj instanceof String) {
            startTimeStr = ((String) startTimeObj).trim();
            if (startTimeStr.isEmpty()) {
                startTimeStr = null;
            }
        }

        // 处理endTime参数
        if (endTimeObj instanceof String) {
            endTimeStr = ((String) endTimeObj).trim();
            if (endTimeStr.isEmpty()) {
                endTimeStr = null;
            }
        }

        LocalDate today = LocalDate.now();

        LocalDate startDate = null;
        LocalDate endDate = null;

        // 解析开始时间
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            try {
                // 尝试解析完整的时间格式 "yyyy-MM-dd HH:mm:ss"
                if (startTimeStr.length() == 19 && startTimeStr.charAt(4) == '-' && startTimeStr.charAt(13) == ':') {
                    startDate = LocalDate.parse(startTimeStr.substring(0, 10));
                }
                // 尝试解析日期格式 "yyyy-MM-dd"
                else if (startTimeStr.length() == 10 && startTimeStr.charAt(4) == '-') {
                    startDate = LocalDate.parse(startTimeStr);
                }
                // 其他格式尝试解析
                else {
                    startDate = LocalDate.parse(startTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception e) {
                log.warn("Invalid startTime format: {}", startTimeStr);
                // 格式错误则忽略该参数
                startDate = null;
            }
        }

        // 解析结束时间
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            try {
                // 尝试解析完整的时间格式 "yyyy-MM-dd HH:mm:ss"
                if (endTimeStr.length() == 19 && endTimeStr.charAt(4) == '-' && endTimeStr.charAt(13) == ':') {
                    endDate = LocalDate.parse(endTimeStr.substring(0, 10));
                }
                // 尝试解析日期格式 "yyyy-MM-dd"
                else if (endTimeStr.length() == 10 && endTimeStr.charAt(4) == '-') {
                    endDate = LocalDate.parse(endTimeStr);
                }
                // 其他格式尝试解析
                else {
                    endDate = LocalDate.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception e) {
                log.warn("Invalid endTime format: {}", endTimeStr);
                // 格式错误则忽略该参数
                endDate = null;
            }
        }

        // 如果没有提供开始时间，默认不限制开始时间
        if (startDate == null) {
            params.put("startTime", null);
        } else {
            // 如果只提供了日期，设置为当天的00:00:00
            if (startTimeStr != null && startTimeStr.length() <= 10) {
                params.put("startTime", startDate.toString() + " 00:00:00");
            } else {
                params.put("startTime", startTimeStr);
            }
        }

        // 如果没有提供结束时间，默认不限制结束时间
        if (endDate == null) {
            params.put("endTime", null);
        } else {
            // 如果结束时间晚于今天，设置为今天
            if (endDate.isAfter(today)) {
                endDate = today;
            }

            // 如果开始时间不为空且结束时间早于开始时间，设置结束时间等于开始时间
            if (startDate != null && endDate.isBefore(startDate)) {
                endDate = startDate;
            }

            // 如果只提供了日期，设置为当天的23:59:59
            if (endTimeStr != null && endTimeStr.length() <= 10) {
                params.put("endTime", endDate.toString() + " 23:59:59");
            } else {
                // 如果提供了完整时间，则使用用户提供的具体时间
                params.put("endTime", endTimeStr);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getTextbookReadingRank(Map<String, Object> params) {
        // 处理时间参数
        processTimeParams(params);
        return systemDataStatisticsMapper.getTextbookReadingRank(params);
    }

    @Override
    public List<Map<String, Object>> getTextbookTypeReadingRank(Map<String, Object> params) {
        // 处理时间参数
        processTimeParams(params);
        return systemDataStatisticsMapper.getTextbookTypeReadingRank(params);
    }

    @Override
    public List<ChapterMasteryVO> getStudentChapterMastery(Long studentId, Long textbookId) {
        List<Map<String, Object>> rawData = systemDataStatisticsMapper.getStudentChapterMastery(studentId, textbookId);
        List<ChapterMasteryVO> result = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("#.##");

        for (Map<String, Object> item : rawData) {
            ChapterMasteryVO vo = new ChapterMasteryVO();
            vo.setChapterId((Long) item.get("chapterId"));

            // 清理章节名称中的HTML标签
            String chapterName = (String) item.get("chapterName");
            if (chapterName != null) {
                // 移除HTML标签
                chapterName = chapterName.replaceAll("<[^>]+>", "");
                // 处理HTML实体
                chapterName = chapterName.replace("&nbsp;", " ");
                chapterName = chapterName.replace("&amp;", "&");
            }
            vo.setChapterName(chapterName);

            // 检查章节是否有题目
            Object questionCountObj = item.get("questionCount");
            Long questionCount = (questionCountObj instanceof Number) ? ((Number) questionCountObj).longValue() : 0L;

            if (questionCount == null || questionCount == 0) {
                // 该章节没有题目
                vo.setMasteryPercentage("0");
                vo.setMasteryDisplay("该章节没有题目");
            } else {
                // 章节有题目，检查是否有答题记录
                Object masteryPercentageObj = item.get("masteryPercentage");
                Double masteryPercentage = (masteryPercentageObj instanceof Number) ? ((Number) masteryPercentageObj).doubleValue() : -1.0;

                if (masteryPercentage != null && masteryPercentage >= 0) {
                    String percentageStr = df.format(masteryPercentage);
                    vo.setMasteryPercentage(percentageStr);
                    vo.setMasteryDisplay(percentageStr + "%");
                } else {
                    vo.setMasteryPercentage("0");
                    vo.setMasteryDisplay("暂无做题记录");
                }
            }

            result.add(vo);
        }

        return result;
    }



    // TODO:资源使用数据统计
    @Override
    public Map<String, Object> getResourceUsageStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. 总数
        Long totalCount = teachingMaterialsService.count();

        // 2. 公共资源数量
        Long publicCount = teachingMaterialsService.lambdaQuery()
                .eq(TeachingMaterials::getIsPublic, true)
                .count();

        // 3. 私有资源数量
        Long privateCount = totalCount - publicCount;

        // 4. 按类型统计（假设 TeachingMaterials 有 type 字段）
        List<Map<String, Object>> typeStatistics = teachingMaterialsMapper.selectMaps(
                new QueryWrapper<TeachingMaterials>()
                        .select("type, COUNT(*) as count")
                        .groupBy("type")
        );

        // 5. 总文件大小（假设 TeachingMaterials 有 size 字段，单位 KB）
        Double totalSize = teachingMaterialsService.lambdaQuery()
                .select(TeachingMaterials::getFileSize)
                .list()
                .stream()
                .filter(tm -> tm != null && tm.getFileSize() != null) // 过滤掉fileSize为null的对象
                .mapToDouble(TeachingMaterials::getFileSize)
                .sum();

        result.put("totalCount", totalCount);
        result.put("publicCount", publicCount);
        result.put("privateCount", privateCount);
        result.put("typeStatistics", typeStatistics);
        result.put("totalSizeMB", totalSize);

        return result;
    }

    /**
     * 获取教材更新申请记录
     */
    @Override
    public IPage<TextbookUpdateApplicationParam> getTextbookUpdateApplications(Page<TextbookUpdateApplicationParam> page) {
        // 直接将分页参数传递给 Mapper 层
        return systemDataStatisticsMapper.getTextbookUpdateApplications(page);
    }
}