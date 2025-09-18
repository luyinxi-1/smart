package com.upc.modular.questionbank.controller.param;

import lombok.Data;

@Data
public class PendingReviewRawDataVO {
    // 题目信息
    private Long questionId;
    private String questionContent;
    private String correctAnswer;
    private String answerAnalysis;
    // 学生回答信息
    private Long contentId;
    private Long recordId;
    private String studentName;
    private String studentAnswer;
}
