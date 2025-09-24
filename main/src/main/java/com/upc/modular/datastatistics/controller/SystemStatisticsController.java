package com.upc.modular.datastatistics.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.VisitorCountDTO;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @ApiOperation("按时间统计访问人数")
    @GetMapping("/visitorCountByTime")
    public ResponseEntity<List<VisitorCountDTO>> getStudentVisitorCountByTime(
            @RequestParam(defaultValue = "week") String timeRange) {

        log.info("API Request: Count visitors for timeRange='{}'", timeRange);

        // 直接将参数传递给 Service 层，由 Service 层处理所有业务逻辑
        List<VisitorCountDTO> result = systemStatisticsService.getStudentVisitorCountByTime(timeRange);

        return ResponseEntity.ok(result);
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

    @ApiOperation("根据时间范围查询总学习时长(单位:秒)")
    @GetMapping("/studyDurationByTime")
    public R<Long> getStudyDurationBytime(
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            // 调用 service 层的新方法
            Long duration = systemStatisticsService.getStudyDurationByTimeRange(startTime, endTime);
            return R.ok(duration);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取学习时长失败: " + e.getMessage());
        }
    }
    @ApiOperation("学生数量统计")
    @GetMapping("/student-count")
    public R<Long> getStudentCount() {
        // TODO: 实现学生数量统计逻辑
        return R.ok(systemStatisticsService.getStudentCount());
    }

    @ApiOperation("教师数量统计")
    @GetMapping("/teacher-count")
    public R<Long> getTeacherCount() {
        // TODO: 实现教师数量统计逻辑
        return R.ok(systemStatisticsService.getTeacherCount());
    }

    @ApiOperation("教学思政数量统计")
    @GetMapping("/ideological-education-count")
    public R<Long> getIdeologicalEducationCount() {
        // TODO: 实现教学思政数量统计逻辑
        return R.ok(systemStatisticsService.getIdeologicalEducationCount());
    }

    @ApiOperation("教学活动数量统计")
    @GetMapping("/teaching-activities-count")
    public R<Long> getTeachingActivitiesCount() {
        // TODO: 实现教学活动数量统计逻辑
        return R.ok(systemStatisticsService.getTeachingActivitiesCount());
    }

    @ApiOperation("题库数量统计")
    @GetMapping("/question-bank-count")
    public R<Long> getQuestionBankCount() {
        // TODO: 实现题库数量统计逻辑
        return R.ok(systemStatisticsService.getQuestionBankCount());
    }

    @ApiOperation("班级数量统计")
    @GetMapping("/class-count")
    public R<Long> getClassCount() {
        // TODO: 实现班级数量统计逻辑
        return R.ok(systemStatisticsService.getClassCount());
    }

    @ApiOperation("在授课程数量统计")
    @GetMapping("/teaching-course-count")
    public R<Long> getTeachingCourseCount() {
        // TODO: 实现在授课程数量统计逻辑
        return R.ok(systemStatisticsService.getTeachingCourseCount());
    }

    @ApiOperation("智慧教材数量统计")
    @GetMapping("/smart-textbook-count")
    public R<Long> getSmartTextbookCount() {
        // TODO: 实现智慧教材数量统计逻辑
        return R.ok(systemStatisticsService.getSmartTextbookCount());
    }

    @ApiOperation("教材类型统计")
    @GetMapping("/textbook-type-count")
    public R<Map<String, Long>> getTextbookTypeCount() {
        return R.ok(systemStatisticsService.getTextbookTypeCount());
    }

    @ApiOperation("交流反馈数量统计")
    @GetMapping("/communication-feedback-count")
    public R<Long> getCommunicationFeedbackCount() {
        return R.ok(systemStatisticsService.getCommunicationFeedbackCount());
    }
    @ApiOperation("教学素材数量统计")
    @GetMapping("/teaching-materials-count")
    public R<Long> getTeachingMaterialsCount() {
        return R.ok(systemStatisticsService.getTeachingMaterialsCount());
    }

    @ApiOperation("资源使用数据统计")
    @GetMapping("/resource-usage-statistics")
    public R<Map<String, Object>> getResourceUsageStatistics() {
        Map<String, Object> statistics = systemStatisticsService.getResourceUsageStatistics();
        return R.ok(statistics);
    }
}
