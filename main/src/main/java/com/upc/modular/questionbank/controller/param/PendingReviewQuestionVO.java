package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
public class PendingReviewQuestionVO {

    @ApiModelProperty("题目ID")
    private Long questionId;

    @ApiModelProperty("题干")
    private String questionContent;

    @ApiModelProperty("参考答案")
    private String correctAnswer;

    @ApiModelProperty("题目解析")
    private String answerAnalysis;

    @ApiModelProperty("该题目满分")
    private Double maxScore;

    @ApiModelProperty("该题目下所有学生的待批改回答列表")
    private List<StudentAnswerForReviewVO> studentAnswers;
}