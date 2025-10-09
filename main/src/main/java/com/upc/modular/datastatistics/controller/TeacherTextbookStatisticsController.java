package com.upc.modular.datastatistics.controller;

import com.upc.common.responseparam.R;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.context.LoginContextHolder;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.service.ITeacherTextbookStatisticsService;
import com.upc.modular.teacher.service.ITeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端教材数据统计控制器
 */
@RestController
@RequestMapping("/teacher-textbook-statistics")
@Api(tags = "教师端教材数据统计")
public class TeacherTextbookStatisticsController {

    @Autowired
    private ITeacherTextbookStatisticsService teacherTextbookStatisticsService;

    @Autowired
    private ITeacherService teacherService;

    @ApiOperation("获取教师教材统计概览")
    @PostMapping("/statistics-overview")
    public R<List<TextbookStatisticsOverviewParam>> getTeacherTextbookStatisticsOverview() {
        // 从当前登录用户获取教师ID
        UserInfoToRedis currentUser = LoginContextHolder.getUserInfoToRedis();
        if (currentUser == null || currentUser.getId() == null) {
            return R.fail("用户未登录");
        }
        
        Long teacherId = teacherService.getTeacherIdByUserId(currentUser.getId());
        if (teacherId == null) {
            return R.fail("当前用户不是教师");
        }
        
        return R.ok(teacherTextbookStatisticsService.getTeacherTextbookStatisticsOverview(teacherId));
    }

    @ApiOperation("获取教材阅读人员统计")
    @PostMapping("/reader-statistics")
    public R<List<ReaderStatisticsParam>> getTextbookReaderStatistics(@RequestBody TextbookDataStatisticsRequestParam param) {
        return R.ok(teacherTextbookStatisticsService.getTextbookReaderStatistics(param.getTextbookId()));
    }

    @ApiOperation("获取学生做题情况统计")
    @PostMapping("/student-question-answering-statistics")
    public R<List<StudentQuestionAnsweringStatisticsParam>> getStudentQuestionAnsweringStatistics(@RequestBody StudentQuestionAnsweringRequestParam param) {
        return R.ok(teacherTextbookStatisticsService.getStudentQuestionAnsweringStatistics(param.getTextbookId(), param.getStudentId()));
    }

    @ApiOperation("按时间统计交流反馈新增数量")
    @PostMapping("/communication-feedback-by-time")
    public R<List<TextbookTimeStatisticsReturnParam>> getCommunicationFeedbackStatisticsByTime(
            @RequestBody TextbookTimeStatisticsSearchParam param) {
        List<TextbookTimeStatisticsReturnParam> result = teacherTextbookStatisticsService
                .getCommunicationFeedbackStatisticsByTime(param);
        return R.ok(result);
    }

    @ApiOperation("按时间统计阅读时长")
    @PostMapping("/reading-duration-by-time")
    public R<List<TextbookTimeStatisticsReturnParam>> getReadingDurationStatisticsByTime(
            @RequestBody TextbookTimeStatisticsSearchParam param) {
        List<TextbookTimeStatisticsReturnParam> result = teacherTextbookStatisticsService
                .getReadingDurationStatisticsByTime(param);
        return R.ok(result);
    }


    @ApiOperation("获取各章节习题正确率统计")
    @PostMapping("/chapter-question-correct-rate")
    public R<List<ChapterQuestionCorrectRateParam>> getChapterQuestionCorrectRateStatistics(
            @RequestBody TextbookDataStatisticsRequestParam param) {
        List<ChapterQuestionCorrectRateParam> result = teacherTextbookStatisticsService
                .getChapterQuestionCorrectRateStatistics(param.getTextbookId());
        return R.ok(result);
    }
}
