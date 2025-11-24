package com.upc.modular.datastatistics.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.entity.StudentStatisticsData;
import com.upc.modular.datastatistics.service.IStudentDataStatistics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "学生数据统计")
@RestController("/count")
@RequestMapping
public class StudentDataStatisticsController {

    @Autowired
    private IStudentDataStatistics iStudentDataStatistics;

    @ApiOperation("统计学生阅读的教材数量")
    @GetMapping("/reading-textbook")
    public R<Long> countStudentTextbookReading() {
        return R.ok(iStudentDataStatistics.countStudentTextbookReading());
    }

    @ApiOperation("统计学生书架教材数量")
    @GetMapping("/favorite-textbook")
    public R<Long> countStudentTextbookFavorites(){
        return R.ok(iStudentDataStatistics.countStudentFavoritebook());
    }

    @ApiOperation("统计学生参与教学活动数量")
    @GetMapping("/teaching-activity")
    public R<Long> countStudentTeachingActivities(){
        return R.ok(iStudentDataStatistics.countStudentTeachingActivities());
    }

    @ApiOperation("统计学生已完成阅读的教材数量")
    @GetMapping("/read-textbook")
    public R<Long> countStudentTextbookread(){
        return R.ok(iStudentDataStatistics.countStudentTextbookRead());
    }

    @ApiOperation("统计学生参与交流反馈数量")
    @GetMapping("/communication-feedback")
    public R<Long> countStudentCommunicationFeedback(){
        return R.ok(iStudentDataStatistics.countStudentCommunicationFeedback());
    }

    @ApiOperation("统计学生学习笔记数量")
    @GetMapping("/note")
    public R<Long> countStudentnotes(){
        return R.ok(iStudentDataStatistics.countStudentnotes());
    }

    @ApiOperation("统计学生答题题库")
    @GetMapping("/question-bank")
    public R<Long> countStudentQuestionBank(){
        return R.ok(iStudentDataStatistics.countStudentQuestions());

    }

    @ApiOperation("统计学生教材阅读总时长")
    @GetMapping("/reading-time")
    public R<Long> countStudentTextbookReadingTime(){
        return R.ok(iStudentDataStatistics.countStudentTextbookReadingTime());
    }

    @ApiOperation("统计学生教材每月阅读时长")
    @GetMapping("/reading-time-month")
    public R<List<StudentReadingTimeByMonthReturnParam>> countStudentTextbookReadingTimeByMonth(@RequestParam Integer year){
        return R.ok(iStudentDataStatistics.countStudentTextbookReadingTimeByMonth(year));
    }

    @ApiOperation("按时间统计总阅读时长")
    @GetMapping("/reading-time-by-time")
    public R<Long> countStudentTextbookReadingTimeByTime(@RequestParam String startTime, @RequestParam String endTime){
        return R.ok(iStudentDataStatistics.countStudentTextbookReadingTimeByTime(startTime, endTime));
    }

    @ApiOperation("学生阅读时间排行榜")
    @GetMapping("/reading-time-currentyear")
    public R<List<StudentStatisticsData>> countStudentTextbookReadingTimeCurrentYear(){
        return R.ok(iStudentDataStatistics.countStudentCurrentYearTextbookReadingTime());
    }
    @ApiOperation("学生阅读数量排行榜")
    @GetMapping("/reading-textbook-currentyear")
    public R<List<StudentStatisticsData>> countStudentTextbookReadingCurrentYear(){
        return R.ok(iStudentDataStatistics.countStudentCurrentTextbookRead());
    }
    @ApiOperation("统计学生教材阅读排行榜")
    @GetMapping("/textbook-rank")
    public R<List<StudentTextbookRankParam>> countStudentTextbookReadingRank(){
        return R.ok(iStudentDataStatistics.countStudentTextbookReadingRank());
    }


    @ApiOperation("统计学生教材完成度")
    @GetMapping("/textbook-completion")
    public R<List<StudentTextbookCompletionReturnParam>> countStudentTextbookCompletion(){
        return R.ok(iStudentDataStatistics.countStudentTextbookCompetion());
    }

    @ApiOperation("统计学生学习路径")
    @GetMapping("/study-path")
    public R<StudentStudyPathReturnParam> countStudentStudyPath(){
        return R.ok(iStudentDataStatistics.countStudentStudyPath());
    }

    @ApiOperation("学习行为分析")
    @GetMapping("/study-behavior")
    public R<StudentBehaviorReturnParam> countStudentBehavior(
            @RequestParam String startTime,
            @RequestParam String endTime
    ){
        return R.ok(iStudentDataStatistics.analyzeStudentBehavior(startTime,endTime));
    }

    @ApiOperation("个人层面分析报告")
    @GetMapping("/personal-analysis")
    public R<StudentAnalysisReturnParam> countStudentPersonalAnalysis(
            @RequestParam String startTime,
            @RequestParam String endTime
    ){
        return R.ok(iStudentDataStatistics.countStudentPersonalAnalysis(startTime,endTime));
    }

    @ApiOperation("统计指定教材的阅读情况")
    @GetMapping("/textbook-situation")
    public R<StudentTextbookSituationReturnParam> countStudentTextbookSituation(
            @RequestParam Long textbookId
    ){
        return R.ok(iStudentDataStatistics.countStudentTextbookSituation(textbookId));
    }

    @ApiOperation("分页查询学生阅读排名")
    @PostMapping("/reading-rank")
    public R<Page<StudentReadingRankParam>> getStudentReadingRankByPage(
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String studentName,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {

        Page<StudentReadingRankParam> page = iStudentDataStatistics.getStudentReadingRankByPage(groupName, studentName, current, size);
        return R.ok(page);
    }
    @ApiOperation("根据学生ID查询阅读过的教材，按阅读量排名返回")
    @GetMapping("/textbook-rank-by-student")
    public R<List<StudentTextbookRankParam>> countStudentTextbookReadingRankByStudentId(
            @RequestParam Long studentId
    ){
        return R.ok(iStudentDataStatistics.countStudentTextbookReadingRankByStudentId(studentId));
    }

}