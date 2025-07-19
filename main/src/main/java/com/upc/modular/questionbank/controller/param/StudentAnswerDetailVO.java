package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StudentAnswerDetailVO {
    @ApiModelProperty("题目ID")
    private Long teachingQuestionId;

    @ApiModelProperty("题型 (1:单选, 2:多选, 3:判断, 4:填空, 5:问答)")
    private Integer questionType;

    @ApiModelProperty("题目内容")
    private String questionContent;

    @ApiModelProperty("学生作答内容")
    private String studentAnswer;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("学生得分")
    private Double studentScore;

    @ApiModelProperty("该题满分")
    private Double maxScore;

    @ApiModelProperty("是否正确(0:错误；1:正确)")
    private Boolean isCorrect;
}
