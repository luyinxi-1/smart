package com.upc.modular.datastatistics.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.StudentReadingTimeByMonthReturnParam;
import com.upc.modular.datastatistics.controller.param.StudentTextbookCompletionReturnParam;
import com.upc.modular.datastatistics.service.IStudentDataStatistics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("统计学生今年教材阅读时长")
    @GetMapping("/reading-time-currentyear")
    public R<Long> countStudentTextbookReadingTimeCurrentYear(){
        return R.ok(iStudentDataStatistics.countStudentCurrentYearTextbookReadingTime());
    }
    @ApiOperation("统计学生教材完成度")
    @GetMapping("/textbook-completion")
    public R<List<StudentTextbookCompletionReturnParam>> countStudentTextbookCompletion(){
        return R.ok(iStudentDataStatistics.countStudentTextbookCompetion());
    }



}