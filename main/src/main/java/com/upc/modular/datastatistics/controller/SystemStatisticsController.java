package com.upc.modular.datastatistics.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @ApiOperation("按时间统计访问人数")
    @GetMapping("/visitorCountByTime")
    public R<List<VisitorCountDTO>> getStudentVisitorCountByTime(
            @RequestParam(defaultValue = "week") String timeRange) {
        List<VisitorCountDTO> result = systemStatisticsService.getStudentVisitorCountByTime(timeRange);
        return R.ok(result);
    }
    // 今日总学习时长
    @ApiOperation("今日总学习时长")
    @PostMapping("/todayStudyDuration")
    public R<Long> getTodayStudyDuration() {
        try {
            return R.ok(systemStatisticsService.getTodayStudyDuration());
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取今日总学习时长失败: " + e.getMessage());
        }
    }
    @ApiOperation("按时间段获取今日总学习时长统计")
    @PostMapping("/todayStudyDurationByPeriod")
    public R<List<StatisticsDto>> getTodayStudyDurationByPeriod() {
        try {
            return R.ok(systemStatisticsService.getTodayStudyDurationByPeriod());
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
    public R<Map<String, Long>> getAllCounts() {
        return R.ok(systemStatisticsService.getAllCounts());
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
}
