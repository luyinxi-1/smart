package com.upc.modular.datastatistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.datastatistics.controller.param.StudentReadingTimeByMonthReturnParam;
import com.upc.modular.datastatistics.controller.param.StudentTextbookCompletionReturnParam;
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

    Long countStudentTextbookRead();

    List<StudentStatisticsData> countStudentCurrentYearTextbookReadingTime();

    List<StudentStatisticsData> countStudentCurrentTextbookRead();

}
