package com.upc.modular.questionbank.controller.param;

import com.upc.modular.questionbank.entity.TeachingQuestion;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "TeachingQuestionWithCreator对象", description = "包含创建人姓名的题目信息")
public class TeachingQuestionWithCreatorDto {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("题型")
    private Integer type;

    @ApiModelProperty("题目内容")
    private String content;

    @ApiModelProperty("难度等级")
    private Integer difficulty;

    @ApiModelProperty("状态（0禁用，1启用）")
    private Integer status;

    @ApiModelProperty("题目答案")
    private String answer;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private java.time.LocalDateTime addDatetime;

    @ApiModelProperty("操作人")
    private Long operator;

    @ApiModelProperty("操作时间")
    private java.time.LocalDateTime operationDatetime;

    @ApiModelProperty("所属学科")
    private String subject;

    @ApiModelProperty("题目分类")
    private String teachingQuestionClassificationId;

    @ApiModelProperty("选择题选项")
    private String choiceQuestionOptions;

    @ApiModelProperty("答案解析")
    private String answerAnalysis;

    @ApiModelProperty("创建人姓名")
    private String creatorName;

    public void copyFrom(TeachingQuestion teachingQuestion) {
        this.id = teachingQuestion.getId();
        this.type = teachingQuestion.getType();
        this.content = teachingQuestion.getContent();
        this.difficulty = teachingQuestion.getDifficulty();
        this.status = teachingQuestion.getStatus();
        this.answer = teachingQuestion.getAnswer();
        this.creator = teachingQuestion.getCreator();
        this.addDatetime = teachingQuestion.getAddDatetime();
        this.operator = teachingQuestion.getOperator();
        this.operationDatetime = teachingQuestion.getOperationDatetime();
        this.subject = teachingQuestion.getSubject();
        this.teachingQuestionClassificationId = teachingQuestion.getTeachingQuestionClassificationId();
        this.choiceQuestionOptions = teachingQuestion.getChoiceQuestionOptions();
        this.answerAnalysis = teachingQuestion.getAnswerAnalysis();
    }
}

