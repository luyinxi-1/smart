package com.upc.modular.datastatistics.service;

import com.upc.modular.datastatistics.controller.param.StudentReadingTimeByMonthReturnParam;
import com.upc.modular.datastatistics.controller.param.StudentTextbookCompletionReturnParam;

import java.util.List;

public interface IStudentDataStatistics {
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
}
