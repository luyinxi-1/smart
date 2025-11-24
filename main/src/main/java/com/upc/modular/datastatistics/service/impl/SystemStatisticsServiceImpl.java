package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.requestparam.PageBaseSearchParam;
import com.upc.common.responseparam.PageBaseReturnParam;
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
import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import com.upc.modular.teachingactivities.service.IDiscussionTopicReplyService;
import com.upc.modular.teachingactivities.service.IDiscussionTopicService;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.service.IIdeologicalMaterialService;
import com.upc.modular.textbook.service.ITextbookService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import com.upc.common.utils.UserInfoToRedis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
    public List<DailyStudyDurationDto> getStudyDurationByTime(String timeRange) {
        // 定义业务所需的时区为一个常量，这是最佳实践
        final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

        // 确保 endDate 也是基于业务时区生成的
        LocalDate endDate = LocalDate.now(BUSINESS_ZONE_ID);
        LocalDate startDate;

        // 1. 日期范围计算... (这部分不变)
        switch (timeRange.toLowerCase()) {
            case "week":
                startDate = endDate.minusDays(6);
                break;
            case "month":
                startDate = endDate.minusMonths(1).plusDays(1);
                break;
            case "year":
                startDate = endDate.minusYears(1).plusDays(1);
                break;
            default:
                throw new IllegalArgumentException("无效的时间范围: " + timeRange);
        }

        // 2. 从 Mapper 获取数据 (不变)
        List<DailyStudyDurationDto> recordedDurations = systemDataStatisticsMapper.getStudyDurationsByDateRange(startDate, endDate);

        // 3. 将结果转换为 Map，使用统一的业务时区
        Map<LocalDate, Long> durationMapInSeconds = recordedDurations.stream()
                .collect(Collectors.toMap(
                        // 使用业务时区进行转换，与SQL的逻辑保持一致
                        dto -> dto.getDate().toInstant().atZone(BUSINESS_ZONE_ID).toLocalDate(),
                        dto -> dto.getDurationInSeconds()
                ));

        // 4. 生成完整的日期序列并补全数据
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        return Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(daysBetween)
                .map(date -> {
                    Long durationInSeconds = durationMapInSeconds.getOrDefault(date, 0L);
                    DailyStudyDurationDto resultDto = new DailyStudyDurationDto();

                    // 在将 LocalDate 转回 Date 时，也应指定业务时区
                    resultDto.setDate(Date.from(date.atStartOfDay(BUSINESS_ZONE_ID).toInstant()));
                    resultDto.setDurationInMinutes(durationInSeconds / 60);
                    resultDto.setDurationInSeconds(durationInSeconds);

                    return resultDto;
                })
                .collect(Collectors.toList());
    }
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
/*@Override
public SystemAllCountsDto getAllCounts(String dateStr) { // 1. 修改返回类型
    SystemAllCountsDto countsDto = new SystemAllCountsDto(); // 2. 创建DTO对象

    LocalDate targetDate;
    if (dateStr != null && !dateStr.trim().isEmpty()) {
        try {
            targetDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            // 如果日期格式不正确，可以记录日志并使用默认值
            targetDate = LocalDate.now();
        }
    } else {
        targetDate = LocalDate.now();
    }
    // 3. 使用setter方法为DTO的每个属性赋值
    countsDto.setTeacherCount(teacherService.count());
    countsDto.setStudentCount(studentService.count());
    countsDto.setGroupCount(groupService.count());
    countsDto.setTeachingideologicalMaterialCount(ideologicalMaterialService.count());

    Long discussionTopicCount = discussionTopicService.lambdaQuery()
            .eq(DiscussionTopic::getIdentityType, 1)
            .count();
    countsDto.setDiscussionTopicCount(discussionTopicCount);

    countsDto.setDiscussionTopicReplyCount(discussionTopicReplyService.count());
    countsDto.setTeachingQuestionBankCount(teachingQuestionbankService.count());
    countsDto.setCourseCount(courseService.count());
    countsDto.setTeachingMaterialsCount(teachingMaterialsService.count());

    Long textbookCount = textbookService.lambdaQuery()
            .eq(Textbook::getReleaseStatus, "1")
            .count();
    countsDto.setTextbookCount(textbookCount);

    *//*Long todayStudyTimeInSeconds = systemDataStatisticsMapper.getTodayStudyDuration();
    Long todayStudyTimeInMinutes = todayStudyTimeInSeconds != null ? todayStudyTimeInSeconds / 60 : 0L;
    countsDto.setTodayStudyTime(todayStudyTimeInMinutes);

    countsDto.setTodayVisitorCount(systemDataStatisticsMapper.getTodayVisitorCount());*//*
    // 4. 调用mapper方法时传入目标日期
    Long studyTimeInSeconds = systemDataStatisticsMapper.getStudyDurationByDate(targetDate);
    countsDto.setTodayStudyTime(studyTimeInSeconds != null ? studyTimeInSeconds / 60 : 0L);

    countsDto.setTodayVisitorCount(systemDataStatisticsMapper.getVisitorCountByDate(targetDate));


    return countsDto; // 4. 返回DTO对象
}*/
@Override
public SystemAllCountsDto getAllCounts(String dateStr) {
    SystemAllCountsDto countsDto = new SystemAllCountsDto();

    LocalDate targetDate;
    if (dateStr != null && !dateStr.trim().isEmpty()) {
        try {
            targetDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            targetDate = LocalDate.now();
        }
    } else {
        targetDate = LocalDate.now();
    }

    countsDto.setTeacherCount(teacherService.count());
    countsDto.setStudentCount(studentService.count());
    countsDto.setGroupCount(groupService.count());
    countsDto.setTeachingideologicalMaterialCount(ideologicalMaterialService.count());

    Long discussionTopicCount = discussionTopicService.lambdaQuery()
            .eq(DiscussionTopic::getIdentityType, 1)
            .count();
    countsDto.setDiscussionTopicCount(discussionTopicCount);

    countsDto.setDiscussionTopicReplyCount(discussionTopicReplyService.count());
    countsDto.setTeachingQuestionBankCount(teachingQuestionbankService.count());
    countsDto.setCourseCount(courseService.count());
    countsDto.setTeachingMaterialsCount(teachingMaterialsService.count());

    Long textbookCount = textbookService.lambdaQuery()
            .eq(Textbook::getReleaseStatus, "1")
            .count();
    countsDto.setTextbookCount(textbookCount);

    // 核心修改：创建Map来传递日期范围参数
    Map<String, Object> dateParams = new HashMap<>();
    dateParams.put("startDate", targetDate);
    dateParams.put("endDate", targetDate.plusDays(1));

    // 使用Map参数调用Mapper方法
    Long studyTimeInSeconds = systemDataStatisticsMapper.getStudyDurationByDate(dateParams);
    countsDto.setTodayStudyTime(studyTimeInSeconds != null ? studyTimeInSeconds / 60 : 0L);

    // 使用Map参数调用Mapper方法
    countsDto.setTodayVisitorCount(systemDataStatisticsMapper.getVisitorCountByDate(dateParams));

    return countsDto;
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
        // TODO: 实现教学素材数量逻辑
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
            }
            // 如果提供了完整时间，则使用用户提供的具体时间
            else {
                params.put("startTime", startTimeStr);
            }
        }

        // 如果没有提供结束时间，默认不限制结束时间
        if (endDate == null) {
            params.put("endTime", null);
        }
        else {
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
            }
            // 如果提供了完整时间，则使用用户提供的具体时间
            else {
                params.put("endTime", endTimeStr);
            }
        }
    }

    @Override
    public PageBaseReturnParam<Map<String, Object>> getTextbookReadingRank(Map<String, Object> params, PageBaseSearchParam pageParam) {
        // 处理时间参数
        processTimeParams(params);
        
        // 创建MyBatis-Plus的Page对象
        Page<Map<String, Object>> page = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        
        // 调用Mapper进行分页查询
        IPage<Map<String, Object>> resultPage = systemDataStatisticsMapper.getTextbookReadingRank(page, params);
        
        return PageBaseReturnParam.ok(resultPage);
    }

    @Override
    public List<Map<String, Object>> getTextbookTypeReadingRank(Map<String, Object> params) {
        // 处理时间参数
        processTimeParams(params);
        return systemDataStatisticsMapper.getTextbookTypeReadingRank(params);
    }

    @Override
    public void exportTextbookTypeReadingRank(HttpServletResponse response) throws Exception {
        try {
            List<Map<String, Object>> rawData = getTextbookTypeReadingRank(null);
            
            // 转换为导出参数
            List<TextbookTypeReadingRankExportParam> exportData = new java.util.ArrayList<>();
            int rank = 1;
            for (Map<String, Object> item : rawData) {
                TextbookTypeReadingRankExportParam param = new TextbookTypeReadingRankExportParam();
                param.setTypeName((String) item.get("typeName"));
                param.setReadingDuration(((Number) item.get("readingDuration")).longValue());
                param.setRank(rank++);
                exportData.add(param);
            }
            
            // 设置响应头
            String fileName = "类型阅读时长排名.xlsx";
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            // 兼容不同浏览器的文件名编码
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName);
            
            // 导出Excel
            com.alibaba.excel.EasyExcel.write(response.getOutputStream(), TextbookTypeReadingRankExportParam.class)
                    .sheet("类型阅读时长排名")
                    .doWrite(exportData);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            throw new RuntimeException("导出失败，请重试");
        }
    }
    /**
     * 私有辅助方法：获取并封装数据
     * 目的：让Excel、PDF、Image共用同一套数据源，避免逻辑重复
     */
    private List<TextbookTypeReadingRankExportParam> getRankExportData() {
        // 获取原始数据 (假设您这里调用的是 Mapper 或其他 Service)
        List<Map<String, Object>> rawData = getTextbookTypeReadingRank(null);

        List<TextbookTypeReadingRankExportParam> exportData = new ArrayList<>();
        if (rawData != null) {
            int rank = 1;
            for (Map<String, Object> item : rawData) {
                TextbookTypeReadingRankExportParam param = new TextbookTypeReadingRankExportParam();
                // 注意空指针安全处理
                param.setTypeName(item.get("typeName") != null ? (String) item.get("typeName") : "未知类型");
                param.setReadingDuration(item.get("readingDuration") != null ? ((Number) item.get("readingDuration")).longValue() : 0L);
                param.setRank(rank++);
                exportData.add(param);
            }
        }
        return exportData;
    }
    @Override
    public void exportTextbookTypeReadingRankPdf(HttpServletResponse response) throws Exception {
        try {
            List<TextbookTypeReadingRankExportParam> list = getRankExportData();

            // 设置响应头
            String fileName = "类型阅读时长排名.pdf";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName);

            // 创建 PDF 文档
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // 字体设置 (解决中文乱码)
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font headFont = new Font(bfChinese, 12, Font.BOLD);
            Font textFont = new Font(bfChinese, 12, Font.NORMAL);

            // 标题
            Paragraph title = new Paragraph("类型阅读时长排名", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 表格 (3列: 排名, 类型名称, 阅读时长)
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{20f, 50f, 30f}); // 列宽比例

            // 表头
            String[] headers = {"排名", "类型名称", "阅读时长"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // 内容
            for (TextbookTypeReadingRankExportParam data : list) {
                PdfPCell c1 = new PdfPCell(new Paragraph(String.valueOf(data.getRank()), textFont));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell c2 = new PdfPCell(new Paragraph(data.getTypeName(), textFont));

                // 可以在这里格式化时长，例如加上 "分钟" 或 "小时"
                PdfPCell c3 = new PdfPCell(new Paragraph(String.valueOf(data.getReadingDuration()), textFont));
                c3.setHorizontalAlignment(Element.ALIGN_CENTER);

                table.addCell(c1);
                table.addCell(c2);
                table.addCell(c3);
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            handleExportError(response, e);
        }
    }

    // ================== 3. 新增 图片 导出 ==================
    @Override
    public void exportTextbookTypeReadingRankImage(HttpServletResponse response) throws Exception {
        try {
            List<TextbookTypeReadingRankExportParam> list = getRankExportData();

            // 设置响应头
            String fileName = "类型阅读时长排名.png";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setContentType("image/png");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName);

            // 计算图片尺寸
            int rowHeight = 40;
            int headerHeight = 60; // 标题高度
            int tableHeadHeight = 40; // 表头高度
            int margin = 40;
            int width = 800;
            int height = headerHeight + tableHeadHeight + (list.size() * rowHeight) + margin * 2;

            // 创建画板
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // 1. 背景白底
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 2. 绘制主标题
            g2d.setColor(java.awt.Color.BLACK);
            // 注意：Linux下需确保有 SimHei 字体
            g2d.setFont(new java.awt.Font("SimHei", java.awt.Font.BOLD, 24));
            String title = "类型阅读时长排名";
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (width - titleWidth) / 2, 50);

            // 3. 绘制表头
            int y = headerHeight + 20;
            int[] colX = {50, 150, 550}; // 列起始X坐标：排名, 名称, 时长

            g2d.setFont(new java.awt.Font("SimHei", java.awt.Font.BOLD, 16));
            g2d.setColor(new java.awt.Color(240, 240, 240)); // 表头背景灰
            g2d.fillRect(40, y - 25, width - 80, rowHeight);

            g2d.setColor(java.awt.Color.BLACK);
            g2d.drawString("排名", colX[0], y);
            g2d.drawString("类型名称", colX[1], y);
            g2d.drawString("阅读时长", colX[2], y);

            // 绘制表头下横线
            g2d.drawLine(40, y + 15, width - 40, y + 15);

            // 4. 绘制数据行
            g2d.setFont(new java.awt.Font("SimHei", java.awt.Font.PLAIN, 16));
            y += rowHeight;

            for (TextbookTypeReadingRankExportParam data : list) {
                g2d.drawString(String.valueOf(data.getRank()), colX[0], y);
                g2d.drawString(data.getTypeName(), colX[1], y);
                g2d.drawString(String.valueOf(data.getReadingDuration()), colX[2], y);

                // 虚线或浅色分割线
                g2d.setColor(new java.awt.Color(230, 230, 230));
                g2d.drawLine(40, y + 15, width - 40, y + 15);
                g2d.setColor(java.awt.Color.BLACK); // 恢复文字颜色

                y += rowHeight;
            }

            // 绘制外边框
            g2d.setColor(java.awt.Color.GRAY);
            g2d.drawRect(40, headerHeight - 5, width - 80, height - headerHeight - margin);

            g2d.dispose();
            ImageIO.write(image, "png", response.getOutputStream());

        } catch (Exception e) {
            handleExportError(response, e);
        }
    }

    /**
     * 统一异常处理
     */
    private void handleExportError(HttpServletResponse response, Exception e) throws java.io.IOException {
        e.printStackTrace();
        if (!response.isCommitted()) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println("{\"code\": 500, \"msg\": \"导出失败: " + e.getMessage() + "\"}");
        }
    }

    // 假设您这个接口方法在Service中也有定义，保留它以兼容原代码引用
    public List<Map<String, Object>> getTextbookTypeReadingRank(Object param) {
        // 这里应该是调用 Mapper 或 DAO 的逻辑
        // 为了编译通过，这里返回空列表或示例数据
        return new ArrayList<>();
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

    @Override
    public IPage<TeacherTextbookPopularityParam> getSystemTextbookPopularity(Page<TeacherTextbookPopularityParam> page) {
        IPage<Map<String, Object>> rawDataPage = systemDataStatisticsMapper.getSystemTextbookPopularity(page);
        List<TeacherTextbookPopularityParam> resultList = new ArrayList<>();
        long rankStart = (page.getCurrent() - 1) * page.getSize() + 1;

        for (Map<String, Object> data : rawDataPage.getRecords()) {
            TeacherTextbookPopularityParam param = new TeacherTextbookPopularityParam();
            param.setRank((int) rankStart++);
            param.setTextbookId(getLongValue(data.get("textbookId")));
            param.setTextbookName((String) data.get("textbookName"));
            param.setReaderCount(getLongValue(data.get("readerCount")));
            param.setReadingDurationMinutes(getLongValue(data.get("readingDurationMinutes")));
            param.setTeachingActivityCount(getLongValue(data.get("teachingActivityCount")));
            param.setCommunicationFeedbackCount(getLongValue(data.get("communicationFeedbackCount")));
            param.setPopularityScore(getIntValue(data.get("popularityScore")));
            resultList.add(param);
        }

        IPage<TeacherTextbookPopularityParam> resultPage = new Page<>(rawDataPage.getCurrent(), rawDataPage.getSize(), rawDataPage.getTotal());
        resultPage.setRecords(resultList);
        return resultPage;
    }

    @Override
    public List<TeacherTextbookPopularityParam> exportSystemTextbookPopularity() {
        List<Map<String, Object>> rawData = systemDataStatisticsMapper.getSystemTextbookPopularityForExport();
        List<TeacherTextbookPopularityParam> result = new ArrayList<>();
        int rank = 1;

        for (Map<String, Object> data : rawData) {
            TeacherTextbookPopularityParam param = new TeacherTextbookPopularityParam();
            param.setRank(rank++);
            //param.setTextbookId(getLongValue(data.get("textbookId")));
            param.setTextbookName((String) data.get("textbookName"));
            param.setReaderCount(getLongValue(data.get("readerCount")));
            param.setReadingDurationMinutes(getLongValue(data.get("readingDurationMinutes")));
            param.setTeachingActivityCount(getLongValue(data.get("teachingActivityCount")));
            param.setCommunicationFeedbackCount(getLongValue(data.get("communicationFeedbackCount")));
            param.setPopularityScore(getIntValue(data.get("popularityScore")));
            result.add(param);
        }
        return result;
    }

    /**
     * 导出系统统计数据
     * @return 返回一个包含单条系统统计数据的列表。
     */
    @Override
    public List<ExportSystemStatisticsParm> exportSystemStatistics() {
        // 1. 调用您现有的方法来获取所有统计数据。
        //    我们传入 null，这样 getAllCounts 会使用当天的日期作为默认值。
        SystemAllCountsDto countsDto = this.getAllCounts(null);

        // 如果由于某种原因没有获取到数据，返回一个空列表以防出错。
        if (countsDto == null) {
            return Collections.emptyList();
        }

        // 2. 创建用于Excel导出的DTO对象。
        ExportSystemStatisticsParm exportParm = new ExportSystemStatisticsParm();

        // 3. 将从 `getAllCounts` 得到的数据，一一映射到 `exportParm` 对象中。
        exportParm.setTeacherCount(countsDto.getTeacherCount());
        exportParm.setStudentCount(countsDto.getStudentCount());
        exportParm.setGroupCount(countsDto.getGroupCount());
        exportParm.setTeachingideologicalMaterialCount(countsDto.getTeachingideologicalMaterialCount());
        exportParm.setDiscussionTopicCount(countsDto.getDiscussionTopicCount());
        exportParm.setDiscussionTopicReplyCount(countsDto.getDiscussionTopicReplyCount());
        exportParm.setTeachingQuestionBankCount(countsDto.getTeachingQuestionBankCount());
        exportParm.setCourseCount(countsDto.getCourseCount());
        exportParm.setTeachingMaterialsCount(countsDto.getTeachingMaterialsCount());
        exportParm.setTextbookCount(countsDto.getTextbookCount());
       // exportParm.setTodayStudyTime(countsDto.getTodayStudyTime());
        //exportParm.setTodayVisitorCount(countsDto.getTodayVisitorCount());
        return Collections.singletonList(exportParm);
    }
    /**
     * 实现 PDF 导出逻辑
     */
    @Override
    public void exportPdf(OutputStream outputStream) {
        // 1. 获取数据并转换为 Map (方便遍历)
        Map<String, Object> dataMap = getStatisticsDataMap();

        // 2. 创建 PDF 文档
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // 3. 设置字体 (关键：解决中文不显示问题)
            // 使用 STSong-Light 和 UniGB-UCS2-H 需要 iText Asian 支持
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font cellFont = new Font(bfChinese, 12, Font.NORMAL);

            // 4. 添加标题
            Paragraph title = new Paragraph("系统统计数据", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // 5. 创建表格 (2列：项目名，数值)
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80); // 表格宽度
            table.setWidths(new float[]{60f, 40f}); // 列宽比例

            // 6. 填充数据
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                PdfPCell keyCell = new PdfPCell(new Paragraph(entry.getKey(), cellFont));
                PdfPCell valCell = new PdfPCell(new Paragraph(String.valueOf(entry.getValue()), cellFont));

                // 样式设置
                keyCell.setPadding(8f);
                valCell.setPadding(8f);
                keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                valCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                valCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                table.addCell(keyCell);
                table.addCell(valCell);
            }

            document.add(table);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("PDF导出失败", e);
        }
    }

    /**
     * 实现 图片 导出逻辑
     */
    @Override
    public void exportImage(OutputStream outputStream) {
        // 1. 获取数据
        Map<String, Object> dataMap = getStatisticsDataMap();

        // 2. 定义图片尺寸和参数
        int width = 600;
        int rowHeight = 50;
        int headerHeight = 80;
        int height = headerHeight + (dataMap.size() * rowHeight) + 30; // 动态计算高度

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        try {
            // 3. 设置背景色（白色）
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // 开启抗锯齿，让文字更清晰
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 4. 绘制标题
            g2d.setColor(java.awt.Color.BLACK);
            // 注意：Linux服务器如果缺少字体，中文会乱码。建议在服务器安装 simhei.ttf 或使用 JDK 加载字体文件
            java.awt.Font titleFont = new java.awt.Font("SimHei", java.awt.Font.BOLD, 24);
            g2d.setFont(titleFont);

            String title = "系统统计数据";
            FontMetrics fm = g2d.getFontMetrics();
            int titleX = (width - fm.stringWidth(title)) / 2;
            g2d.drawString(title, titleX, 50);

            // 5. 绘制表格和内容
            java.awt.Font contentFont = new java.awt.Font("SimHei", java.awt.Font.PLAIN, 16);
            g2d.setFont(contentFont);

            int startY = headerHeight;
            int padding = 40;

            // 画外框
            g2d.setColor(java.awt.Color.GRAY);
            g2d.drawRect(padding, headerHeight - 20, width - (padding * 2), height - headerHeight - 10);

            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                // 绘制文字
                g2d.setColor(java.awt.Color.BLACK);
                g2d.drawString(entry.getKey(), padding + 20, startY + 10);

                String valueStr = String.valueOf(entry.getValue());
                int valWidth = g2d.getFontMetrics().stringWidth(valueStr);
                g2d.drawString(valueStr, width - padding - 20 - valWidth, startY + 10);

                // 绘制分割线
                g2d.setColor(new java.awt.Color(220, 220, 220));
                g2d.drawLine(padding, startY + 25, width - padding, startY + 25);

                startY += rowHeight;
            }

            // 6. 写入流
            ImageIO.write(image, "png", outputStream);

        } catch (IOException e) {
            throw new RuntimeException("图片导出失败", e);
        } finally {
            g2d.dispose();
        }
    }

    /**
     * 私有辅助方法：获取数据并转换为有序Map，用于给 PDF 和 Image 提供统一的数据源和中文Label
     */
    private Map<String, Object> getStatisticsDataMap() {
        List<ExportSystemStatisticsParm> list = this.exportSystemStatistics();
        ExportSystemStatisticsParm data;
        if (list != null && !list.isEmpty()) {
            data = list.get(0);
        } else {
            data = new ExportSystemStatisticsParm(); // 空对象防止空指针
        }

        // 使用 LinkedHashMap 保持插入顺序
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("教师人数", data.getTeacherCount());
        map.put("学生人数", data.getStudentCount());
        map.put("班级数量", data.getGroupCount());
        map.put("教学思政素材数量", data.getTeachingideologicalMaterialCount());
        map.put("讨论话题数量", data.getDiscussionTopicCount());
        map.put("话题回复数量", data.getDiscussionTopicReplyCount());
        map.put("题库数量", data.getTeachingQuestionBankCount());
        map.put("课程数量", data.getCourseCount());
        map.put("教学素材数量", data.getTeachingMaterialsCount());
        map.put("教材数量", data.getTextbookCount());
        return map;
    }

    @Override
    public IPage<TextbookStatisticsOverviewParam> getSystemTextbookStatisticsOverview(Page<TextbookStatisticsOverviewParam> page, UserInfoToRedis currentUser, String textbookName) {
        IPage<Map<String, Object>> rawPage;
        
        // 判断用户类型
        if (currentUser.getUserType() == 0) { // 管理员
            rawPage = systemDataStatisticsMapper.getSystemTextbookStatisticsOverview(page, textbookName);
        } else if (currentUser.getUserType() == 2) { // 教师
            rawPage = systemDataStatisticsMapper.getTeacherTextbookStatisticsOverview(page, currentUser.getId(), textbookName);
        } else { // 其他用户类型，返回空结果
            rawPage = new Page<>(page.getCurrent(), page.getSize(), 0);
        }
        
        return rawPage.convert(data -> {
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
            return param;
        });
    }

    @Override
    public List<TextbookStatisticsOverviewParam> exportSystemTextbookStatisticsOverview(UserInfoToRedis currentUser, String textbookName) {
        List<Map<String, Object>> rawData;
        
        // 判断用户类型
        if (currentUser.getUserType() == 0) { // 管理员
            rawData = systemDataStatisticsMapper.exportSystemTextbookStatisticsOverview(textbookName);
        } else if (currentUser.getUserType() == 2) { // 教师
            rawData = systemDataStatisticsMapper.exportTeacherTextbookStatisticsOverview(currentUser.getId(), textbookName);
        } else { // 其他用户类型，返回空结果
            rawData = new ArrayList<>();
        }
        
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
    public IPage<ReaderStatisticsParam> getReaderStatistics(Page<ReaderStatisticsParam> page, Long textbookId) {
        IPage<Map<String, Object>> rawPage = systemDataStatisticsMapper.getReaderStatistics(page, textbookId);
        return rawPage.convert(data -> {
            ReaderStatisticsParam param = new ReaderStatisticsParam();
            param.setStudentId(getLongValue(data.get("studentId")));
            param.setStudentName((String) data.get("studentName"));
            param.setReadingDuration(getLongValue(data.get("readingDuration")));
            param.setLearningBehavior((String) data.get("learningBehavior"));
            param.setChapterCount(getIntValue(data.get("chapterCount")));
            param.setLastReadingTime(getStringValue(data.get("lastReadingTime")));
            param.setProgressPercentage(getDoubleValue(data.get("progressPercentage")));
            return param;
        });
    }

    @Override
    public List<TextbookTimeStatisticsReturnParam> getReadingDurationStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        log.info("按时间统计阅读时长 (Admin)，参数: {}", param);
        List<TextbookTimeStatisticsReturnParam> result = calculateReadingDurationStatisticsByTime(param);
        log.info("按时间统计阅读时长 (Admin) 完成，结果数量: {}", result.size());
        return result;
    }

    /**
     * 按时间统计阅读时长 - (Copied from TeacherTextbookStatisticsServiceImpl)
     * @param param 搜索参数
     * @return 时间统计结果
     */
    private List<TextbookTimeStatisticsReturnParam> calculateReadingDurationStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        // 容忍范围
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;

        // 参数校验
        if (param.getTextbookId() == null) {
            throw new IllegalArgumentException("教材ID不能为空");
        }
        
        if (param.getTimeRange() == null || param.getTimeRange().isEmpty()) {
            throw new IllegalArgumentException("时间范围不能为空");
        }

        // 获取学习日志记录，只获取data_type=0的记录（有效阅读行为）
        List<LearningLog> records;
        String[] timeRange = getTimeRangeByType(param.getTimeRange());
        if (timeRange != null) {
            records = systemDataStatisticsMapper.findLearningLogsByTextbookIdAndTime(
                    param.getTextbookId(), timeRange[0], timeRange[1]).stream()
                    .filter(log -> log.getDataType() == 0)
                    .collect(Collectors.toList());
        }
        else {
            records = systemDataStatisticsMapper.findLearningLogsByTextbookId(param.getTextbookId()).stream()
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

        // 按时间排序
        userRecordsMap.values().forEach(userRecords -> userRecords.sort(Comparator.comparing(LearningLog::getAddDatetime)));

        // 按时间维度统计
        Map<String, Long> timeReadingMap = new HashMap<>();

        for (Map.Entry<Long, List<LearningLog>> entry : userRecordsMap.entrySet()) {
            List<LearningLog> userRecords = entry.getValue();

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

    @Override
    public List<TextbookTimeStatisticsReturnParam> getCommunicationFeedbackStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        log.info("按时间统计交流反馈新增数量 (Admin)，参数: {}", param);
        List<TextbookTimeStatisticsReturnParam> result = calculateCommunicationFeedbackStatisticsByTime(param);
        log.info("按时间统计交流反馈新增数量 (Admin) 完成，结果数量: {}", result.size());
        return result;
    }

    /**
     * 按时间统计交流反馈数量 - (Copied from TeacherTextbookStatisticsServiceImpl)
     * @param param 搜索参数
     * @return 时间统计结果
     */
    private List<TextbookTimeStatisticsReturnParam> calculateCommunicationFeedbackStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        // 获取交流反馈记录
        List<DiscussionTopicReply> records;
        if (param.getTimeRange() != null && !param.getTimeRange().isEmpty()) {
            String[] timeRange = getTimeRangeByType(param.getTimeRange());
            if (timeRange != null) {
                records = systemDataStatisticsMapper.findCommunicationFeedbackByTextbookIdAndTime(
                        param.getTextbookId(), timeRange[0], timeRange[1]);
            }
            else {
                records = systemDataStatisticsMapper.findCommunicationFeedbackByTextbookId(param.getTextbookId());
            }
        }
        else {
            records = systemDataStatisticsMapper.findCommunicationFeedbackByTextbookId(param.getTextbookId());
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

    @Override
    public List<ChapterQuestionCorrectRateParam> getChapterQuestionCorrectRateStatistics(Long textbookId) {
        List<Map<String, Object>> rawData = systemDataStatisticsMapper.getChapterQuestionCorrectRateStatistics(textbookId);
        List<ChapterQuestionCorrectRateParam> result = new ArrayList<>();

        for (Map<String, Object> item : rawData) {
            ChapterQuestionCorrectRateParam param = new ChapterQuestionCorrectRateParam();
            param.setChapterId(getLongValue(item.get("chapterId")));
            param.setChapterName((String) item.get("chapterName"));
            param.setChapterLevel(getIntValue(item.get("chapterLevel")));
            param.setParentChapterId(getLongValue(item.get("parentChapterId")));
            param.setQuestionBankCount(getLongValue(item.get("questionBankCount")));
            param.setTotalAnswerCount(getLongValue(item.get("totalAnswerCount")));
            param.setCorrectAnswerCount(getLongValue(item.get("correctAnswerCount")));
            param.setCorrectRate(getDoubleValue(item.get("correctRate")));
            result.add(param);
        }
        return result;
    }

    /**
     * 根据时间范围类型获取开始和结束时间 - (Copied from TeacherTextbookStatisticsServiceImpl)
     * @param timeRange 时间范围类型
     * @return 包含开始时间和结束时间的数组 [startTime, endTime]
     */
    private String[] getTimeRangeByType(String timeRange) {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        switch (timeRange.toLowerCase()) {
            case "week":
                LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
                return new String[]{startOfWeek.format(formatter), now.format(formatter)};
            case "month":
                LocalDate startOfMonth = now.withDayOfMonth(1);
                return new String[]{startOfMonth.format(formatter), now.format(formatter)};
            case "year":
                LocalDate startOfYear = now.withDayOfYear(1);
                return new String[]{startOfYear.format(formatter), now.format(formatter)};
            default:
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
     * 根据时间范围类型格式化时间 - (Copied from TeacherTextbookStatisticsServiceImpl)
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

    // ==================== Private Helper Methods ====================

    private Long getLongValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return 0L; // Default to 0 if null or not a number
    }

    private Integer getIntValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0; // Default to 0 if null or not a number
    }

    private Double getDoubleValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return 0.0; // Default to 0.0 if null or not a number
    }

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
