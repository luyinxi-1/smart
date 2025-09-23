package com.upc.modular.datastatistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.entity.StudentStatisticsData;

import java.util.List;

public interface IStudentDataStatistics extends IService<StudentStatisticsData> {
    Long countStudentTextbookReading();

    Long countStudentFavoritebook();

    Long countStudentTeachingActivities();

    Long countStudentCommunicationFeedback();

    Long countStudentnotes();

    Long countStudentQuestions();

    Long countStudentTextbookReadingTime();

    List<StudentReadingTimeByMonthReturnParam> countStudentTextbookReadingTimeByMonth(Integer year);

    List<StudentTextbookCompletionReturnParam> countStudentTextbookCompetion();

    List<StudentTextbookCompletionReturnParam> countStudentTextbookCompetion(String start_time,String end_time);

    Long countStudentTextbookRead();

    Long countStudentTextbookRead(String startTime, String endTime);

    List<StudentStatisticsData> countStudentCurrentYearTextbookReadingTime();

    List<StudentStatisticsData> countStudentCurrentTextbookRead();

    Long countStudentTextbookReadingTimeByTime(String startTime, String endTime);

    StudentStudyPathReturnParam countStudentStudyPath();

    StudentBehaviorReturnParam analyzeStudentBehavior(String startTime, String endTime);

    StudentAnalysisReturnParam countStudentPersonalAnalysis(String startTime, String endTime);

    StudentTextbookSituationReturnParam countStudentTextbookSituation(Long textbookId);
}
