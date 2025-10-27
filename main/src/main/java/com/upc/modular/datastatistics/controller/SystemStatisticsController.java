package com.upc.modular.datastatistics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/system-statistics")
@Api(tags = "系统数据统计")
public class SystemStatisticsController {
    @Autowired
    private ISystemStatisticsService systemStatisticsService;

    @ApiOperation("获取今日访问人数")
    @PostMapping("/todayVisitorCount")
    public R<Long> getTodayVisitorCount() {
        try {
            return R.ok(systemStatisticsService.getTodayVisitorCount());
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取今日访问人数失败: " + e.getMessage());
        }
    }
    @ApiOperation("按时间段获取今日访问人数统计")
    @PostMapping("/todayVisitorCountByPeriod")
    public R<List<StatisticsDto>> getTodayVisitorCountByPeriod() {
        try {
            return R.ok(systemStatisticsService.getTodayVisitorCountByPeriod());
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取今日分时访问人数失败: " + e.getMessage());
        }
    }

    @ApiOperation("按(周/月/年)时间统计访问人数")
    @GetMapping("/visitorCountByTime")
    public R<List<VisitorCountDTO>> getStudentVisitorCountByTime(
            @RequestParam(defaultValue = "week") String timeRange) {
        List<VisitorCountDTO> result = systemStatisticsService.getStudentVisitorCountByTime(timeRange);
        return R.ok(result);
    }
    @ApiOperation("按(周/月/年)统计每日学习时长")
    @GetMapping("/studyDurationByTime")
    public R<List<DailyStudyDurationDto>> getStudyDurationByTime(
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            List<DailyStudyDurationDto> result = systemStatisticsService.getStudyDurationByTime(timeRange);
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取学习时长统计失败: " + e.getMessage());
        }
    }
    // 今日总学习时长
    @ApiOperation("今日总学习时长")
    @PostMapping("/todayStudyDuration")
    public R<Long> getTodayStudyDuration() {
        try {
            Long durationInSeconds = systemStatisticsService.getTodayStudyDuration();
            // 将秒转换为分钟
            Long durationInMinutes = durationInSeconds / 60;
            return R.ok(durationInMinutes);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取今日总学习时长失败: " + e.getMessage());
        }
    }

@ApiOperation("按时间段获取今日总学习时长统计(分钟)")
@PostMapping("/todayStudyDurationByPeriod")
public R<List<StatisticsDto>> getTodayStudyDurationByPeriod() {
    try {
        List<StatisticsDto> result = systemStatisticsService.getTodayStudyDurationByPeriod();
        // 将秒转换为分钟
        result.forEach(dto -> {
            if (dto.getValue() != null) {
                dto.setValue(dto.getValue() / 60);
            }
        });
        return R.ok(result);
    } catch (Exception e) {
        e.printStackTrace();
        return R.fail("获取今日分时总学习时长失败: " + e.getMessage());
    }
}
    @ApiOperation("根据日期范围查询学习趋势(单位:秒)")
    @GetMapping("/studyTrendByDate")
    public R<List<StudyTrendDTO>> getStudyTrendByDate(
            @ApiParam(value = "查询开始日期 (格式: yyyy-MM-dd)", required = true, example = "2022-01-01")
            @RequestParam("startDate") String startDateStr,
            @ApiParam(value = "查询结束日期 (格式: yyyy-MM-dd)", required = true, example = "2022-01-10")
            @RequestParam("endDate") String endDateStr,
            @ApiParam(value = "统计类型: day(按日), week(按周), month(按月)", required = true)
            @RequestParam("type") String type) {
        LocalDate startDate;
        LocalDate endDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            startDate = LocalDate.parse(startDateStr, formatter);
            endDate = LocalDate.parse(endDateStr, formatter);
        } catch (DateTimeParseException e) {
            // 3. 如果格式错误，返回一个友好的错误提示
            return R.fail("日期格式不正确，请确保使用 yyyy-MM-dd 格式。");
        }
        // 4. 调用 Service 层，传入已经成功转换的 LocalDate 对象
        List<StudyTrendDTO> trendData = systemStatisticsService.getStudyTrendByDateRange(startDate, endDate, type);
        return R.ok(trendData);
    }
@ApiOperation("获取系统所有核心数据统计")
@GetMapping("/all-counts")
public R<SystemAllCountsDto> getAllCounts(@RequestParam(value = "date", required = false) String dateStr) {
    return R.ok(systemStatisticsService.getAllCounts(dateStr));
}

    @ApiOperation("教师数量统计")
    @GetMapping("/teacher-count")
    public R<Long> getTeacherCount() {
        // TODO: 实现教师数量统计逻辑
        return R.ok(systemStatisticsService.getTeacherCount());
    }
    @ApiOperation("教学素材数量统计")
    @GetMapping("/teaching-materials-count")
    public R<Long> getTeachingMaterialsCount() {

        return R.ok(systemStatisticsService.getTeachingMaterialsCount());
    }

    @ApiOperation("班级数量统计")
    @GetMapping("/class-count")
    public R<Long> getClassCount() {
        // TODO: 实现班级数量统计逻辑
        return R.ok(systemStatisticsService.getClassCount());
    }

    @ApiOperation("教材类型统计")
    @GetMapping("/textbook-type-count")
    public R<List<TextbookTypeCountDto>> getTextbookTypeCount() {
        return R.ok(systemStatisticsService.getTextbookTypeCount());
    }


    @ApiOperation("交流反馈数量统计")
    @GetMapping("/communication-feedback-count")
    public R<Long> getCommunicationFeedbackCount() {
        return R.ok(systemStatisticsService.getCommunicationFeedbackCount());
    }


    @ApiOperation("资源使用数据统计")
    @GetMapping("/resource-usage-statistics")
    public R<Map<String, Object>> getResourceUsageStatistics() {
        Map<String, Object> statistics = systemStatisticsService.getResourceUsageStatistics();
        return R.ok(statistics);
    }

    @ApiOperation("获取教材更新申请记录")
    @GetMapping("/textbook-update-applications")
    public R<IPage<TextbookUpdateApplicationParam>> getTextbookUpdateApplications(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        Page<TextbookUpdateApplicationParam> page = new Page<>(current, size);
        IPage<TextbookUpdateApplicationParam> applications = systemStatisticsService.getTextbookUpdateApplications(page);
        return R.ok(applications);
    }
    @ApiOperation("获取全系统教材热度排名")
    @GetMapping("/textbook-popularity")
    public R<List<TeacherTextbookPopularityParam>> getSystemTextbookPopularity() {
        return R.ok(systemStatisticsService.getSystemTextbookPopularity());
    }
    @ApiOperation("获取全系统教材统计概览 (分页)")
    @GetMapping("/textbook-overview")
    public R<IPage<TextbookStatisticsOverviewParam>> getSystemTextbookStatisticsOverview(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(value = "sortField", defaultValue = "textbookName") String sortField,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {

        Page<TextbookStatisticsOverviewParam> page = new Page<>(current, size);

        // Add sorting logic
        if (StringUtils.hasText(sortField)) {
            // Convert camelCase to snake_case for database column name
            String sortFieldDb = sortField.replaceAll("([A-Z])", "_$1").toLowerCase();
            if ("asc".equalsIgnoreCase(sortOrder)) {
                page.addOrder(OrderItem.asc(sortFieldDb));
            } else {
                page.addOrder(OrderItem.desc(sortFieldDb));
            }
        }

        return R.ok(systemStatisticsService.getSystemTextbookStatisticsOverview(page));
    }
    @ApiOperation("获取教材阅读人员统计 (分页)")
    @PostMapping("/reader-statistics")
    public R<IPage<ReaderStatisticsParam>> getReaderStatistics(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(value = "sortField", defaultValue = "studentName") String sortField,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestBody TextbookDataStatisticsRequestParam param) {

        Page<ReaderStatisticsParam> page = new Page<>(current, size);

        // Add sorting logic
        if (StringUtils.hasText(sortField)) {
            // 移除 camelCase 到 snake_case 的转换，直接使用 sortField，因为 SQL 别名已是 camelCase
            // String sortFieldDb = sortField.replaceAll("([A-Z])", "_$1").toLowerCase();
            if ("asc".equalsIgnoreCase(sortOrder)) {
                page.addOrder(OrderItem.asc(sortField));
            } else {
                page.addOrder(OrderItem.desc(sortField));
            }
        }

        return R.ok(systemStatisticsService.getReaderStatistics(page, param.getTextbookId()));
    }

    @ApiOperation("按时间统计阅读时长")
    @PostMapping("/reading-duration-by-time")
    public R<List<TextbookTimeStatisticsReturnParam>> getReadingDurationStatisticsByTime(
            @RequestBody @Valid TextbookTimeStatisticsSearchParam param) {
        return R.ok(systemStatisticsService.getReadingDurationStatisticsByTime(param));
    }

    @ApiOperation("按时间统计交流反馈新增数量")
    @PostMapping("/communication-feedback-by-time")
    public R<List<TextbookTimeStatisticsReturnParam>> getCommunicationFeedbackStatisticsByTime(
            @RequestBody TextbookTimeStatisticsSearchParam param) {
        return R.ok(systemStatisticsService.getCommunicationFeedbackStatisticsByTime(param));
    }

    @ApiOperation("获取各章节习题正确率统计")
    @PostMapping("/chapter-question-correct-rate")
    public R<List<ChapterQuestionCorrectRateParam>> getChapterQuestionCorrectRateStatistics(
            @RequestBody TextbookDataStatisticsRequestParam param) {
        return R.ok(systemStatisticsService.getChapterQuestionCorrectRateStatistics(param.getTextbookId()));
    }

    /**
     * 获取指定学生在某教材下各章节的掌握度
     * @param studentId 学生ID
     * @param textbookId 教材ID
     * @return 章节掌握度列表
     */
    @ApiOperation("获取指定学生在某教材下各章节的掌握度")
    @PostMapping("/student-chapter-mastery")
    public R<List<ChapterMasteryVO>> getStudentChapterMastery(
            @ApiParam(value = "学生ID", required = true) @RequestParam Long studentId,
            @ApiParam(value = "教材ID", required = true) @RequestParam Long textbookId) {
        try {
            List<ChapterMasteryVO> result = systemStatisticsService.getStudentChapterMastery(studentId, textbookId);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("获取学生章节掌握度失败: " + e.getMessage());
        }
    }
}