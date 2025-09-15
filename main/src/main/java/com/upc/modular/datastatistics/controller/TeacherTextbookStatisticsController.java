package com.upc.modular.datastatistics.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.service.ITeacherTextbookStatisticsService;
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

    @ApiOperation("获取教材数据统计")
    @PostMapping("/data-statistics")
    public R<TextbookDataStatisticsParam> getTextbookDataStatistics(
            @RequestBody TextbookDataStatisticsRequestParam param) {
        TextbookDataStatisticsParam result = teacherTextbookStatisticsService.getTextbookDataStatistics(param.getTextbookId());
        return R.ok(result);
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
