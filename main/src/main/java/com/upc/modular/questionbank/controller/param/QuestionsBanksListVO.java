package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ApiModel(value = "题库题目信息(带类型描述)", description = "题库题目信息(带类型描述)")
public class QuestionsBanksListVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("题目id")
    private Long questionId;

    @ApiModelProperty("题库id")
    private Long bankId;

    @ApiModelProperty("顺序")
    private Integer sequence;

    @ApiModelProperty("每道题目分值")
    private Double score;

    @ApiModelProperty("题目类型(数字)")
    private Integer questionType;

    @ApiModelProperty("题目类型(字符串描述)")
    private String questionTypeName;

    @ApiModelProperty("题目名称")
    private String questionContent;
    @ApiModelProperty("题目分类名称")
    private String questionClassificationName;

    public static String getQuestionTypeName(Integer type) {
        if (type == null) return "未知题型";

        switch (type) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "问答题";
            default: return "未知题型";
        }
    }
    @ApiModelProperty("难度等级")
    @TableField("difficulty")
    private Integer difficulty;

    @ApiModelProperty("状态（0禁用，1启用）")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("题目答案")
    @TableField("answer")
    private String answer;

    @ApiModelProperty("创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @ApiModelProperty("所属学科")
    @TableField("subject")
    private String subject;

    @ApiModelProperty("选择题选项")
    @TableField("choice_question_options")
    private String choiceQuestionOptions;

    @ApiModelProperty("答案解析")
    @TableField("answer_analysis")
    private String answerAnalysis;
}
