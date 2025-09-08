package com.upc.modular.datastatistics.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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

    // 按时间统计访问人数
    @ApiOperation("按时间统计访问人数")
    @PostMapping("/visitorCountByTime")
    public R<List<Map<String, Object>>> getVisitorCountByTime(@RequestParam Map<String, Object> param) {
        try {
            return R.ok(systemStatisticsService.getVisitorCountByTime(param));
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("按时间统计访问人数失败: " + e.getMessage());
        }
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


    // 按时间统计总学习时长
    @ApiOperation("按时间统计总学习时长")
    @PostMapping("/studyDurationByTime")
    public R<List<Map<String, Object>>> getStudyDurationByTime(@RequestBody Map<String, Object> param) {
        try {
            return R.ok(systemStatisticsService.getStudyDurationByTime(param));
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("按时间统计总学习时长失败: " + e.getMessage());
        }
    }

/*    @ApiOperation("获取今日活跃人数")
    @GetMapping("/today-active-users")
    public R<Integer> getTodayActiveUsers() {
        // TODO: 实现今日活跃人数统计逻辑
        return R.ok();
    }*/
// 今日活跃人数
@ApiOperation("今日活跃人数")
@PostMapping("/todayActiveUserCount")
public R<Long> getTodayActiveUserCount() {
    try {
        return R.ok(systemStatisticsService.getTodayActiveUserCount());
    } catch (Exception e) {
        e.printStackTrace();
        return R.fail("获取今日活跃人数失败: " + e.getMessage());
    }
}

    // 按时间统计活跃人数
    @ApiOperation("按时间统计活跃人数")
    @PostMapping("/activeUserCountByTime")
    public R<List<Map<String, Object>>> getActiveUserCountByTime(@RequestParam Map<String, Object> param) {
        try {
            return R.ok(systemStatisticsService.getActiveUserCountByTime(param));
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("按时间统计活跃人数失败: " + e.getMessage());
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
