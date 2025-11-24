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

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@RestController
@RequestMapping("/datastatistics/student")
@Api(tags = "学生数据统计")
public class StudentDataStatisticsController {

    @Autowired
    private IStudentDataStatistics studentDataStatistics;

    @ApiOperation("统计学生阅读的教材数量")
    @GetMapping("/reading-textbook")
    public R<Long> countStudentTextbookReading() {
        return R.ok(studentDataStatistics.countStudentTextbookReading());
    }

    @ApiOperation("统计学生书架教材数量")
    @GetMapping("/favorite-textbook")
    public R<Long> countStudentTextbookFavorites(){
        return R.ok(studentDataStatistics.countStudentFavoritebook());
    }

    @ApiOperation("统计学生参与教学活动数量")
    @GetMapping("/teaching-activity")
    public R<Long> countStudentTeachingActivities(){
        return R.ok(studentDataStatistics.countStudentTeachingActivities());
    }

    @ApiOperation("统计学生已完成阅读的教材数量")
    @GetMapping("/read-textbook")
    public R<Long> countStudentTextbookread(){
        return R.ok(studentDataStatistics.countStudentTextbookRead());
    }

    @ApiOperation("统计学生参与交流反馈数量")
    @GetMapping("/communication-feedback")
    public R<Long> countStudentCommunicationFeedback(){
        return R.ok(studentDataStatistics.countStudentCommunicationFeedback());
    }

    @ApiOperation("统计学生学习笔记数量")
    @GetMapping("/note")
    public R<Long> countStudentnotes(){
        return R.ok(studentDataStatistics.countStudentnotes());
    }

    @ApiOperation("统计学生答题题库")
    @GetMapping("/question-bank")
    public R<Long> countStudentQuestionBank(){
        return R.ok(studentDataStatistics.countStudentQuestions());

    }

    @ApiOperation("统计学生教材阅读总时长")
    @GetMapping("/reading-time")
    public R<Long> countStudentTextbookReadingTime(){
        return R.ok(studentDataStatistics.countStudentTextbookReadingTime());
    }

    @ApiOperation("统计学生教材每月阅读时长")
    @GetMapping("/reading-time-month")
    public R<List<StudentReadingTimeByMonthReturnParam>> countStudentTextbookReadingTimeByMonth(@RequestParam Integer year){
        return R.ok(studentDataStatistics.countStudentTextbookReadingTimeByMonth(year));
    }

    @ApiOperation("按时间统计总阅读时长")
    @GetMapping("/reading-time-by-time")
    public R<Long> countStudentTextbookReadingTimeByTime(@RequestParam String startTime, @RequestParam String endTime){
        return R.ok(studentDataStatistics.countStudentTextbookReadingTimeByTime(startTime, endTime));
    }

    @ApiOperation("学生阅读时间排行榜")
    @GetMapping("/reading-time-currentyear")
    public R<List<StudentStatisticsData>> countStudentTextbookReadingTimeCurrentYear(){
        return R.ok(studentDataStatistics.countStudentCurrentYearTextbookReadingTime());
    }
    @ApiOperation("学生阅读数量排行榜")
    @GetMapping("/reading-textbook-currentyear")
    public R<List<StudentStatisticsData>> countStudentTextbookReadingCurrentYear(){
        return R.ok(studentDataStatistics.countStudentCurrentTextbookRead());
    }
    @ApiOperation("统计学生教材阅读排行榜")
    @GetMapping("/textbook-rank")
    public R<List<StudentTextbookRankParam>> countStudentTextbookReadingRank(){
        return R.ok(studentDataStatistics.countStudentTextbookReadingRank());
    }


    @ApiOperation("统计学生教材完成度")
    @GetMapping("/textbook-completion")
    public R<List<StudentTextbookCompletionReturnParam>> countStudentTextbookCompletion(){
        return R.ok(studentDataStatistics.countStudentTextbookCompetion());
    }

    @ApiOperation("统计学生学习路径")
    @GetMapping("/study-path")
    public R<StudentStudyPathReturnParam> countStudentStudyPath(){
        return R.ok(studentDataStatistics.countStudentStudyPath());
    }

    @ApiOperation("学习行为分析")
    @GetMapping("/study-behavior")
    public R<StudentBehaviorReturnParam> countStudentBehavior(
            @RequestParam String startTime,
            @RequestParam String endTime
    ){
        return R.ok(studentDataStatistics.analyzeStudentBehavior(startTime,endTime));
    }

    @ApiOperation("个人层面分析报告")
    @GetMapping("/personal-analysis")
    public R<StudentAnalysisReturnParam> countStudentPersonalAnalysis(
            @RequestParam String startTime,
            @RequestParam String endTime
    ){
        return R.ok(studentDataStatistics.countStudentPersonalAnalysis(startTime,endTime));
    }

    @ApiOperation("统计指定教材的阅读情况")
    @GetMapping("/textbook-situation")
    public R<StudentTextbookSituationReturnParam> countStudentTextbookSituation(
            @RequestParam Long textbookId
    ){
        return R.ok(studentDataStatistics.countStudentTextbookSituation(textbookId));
    }

    @ApiOperation("分页查询学生阅读排名")
    @PostMapping("/reading-rank")
    public R<Page<StudentReadingRankParam>> getStudentReadingRankByPage(
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String studentName,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        
        Page<StudentReadingRankParam> page = studentDataStatistics.getStudentReadingRankByPage(groupName, studentName, current, size);
        return R.ok(page);
    }
}