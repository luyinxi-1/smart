package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 教师答题明细表
 * </p>
 *
 * @author [你的名字]
 * @since [当前日期]
 */
@Data
@Accessors(chain = true)
@TableName("teacher_exercises_content")
@ApiModel(value = "TeacherExercisesContent对象", description = "教师答题明细表")
public class TeacherExercisesContent implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教师id")
    @TableField("teacher_id")
    private Long teacherId;

    @ApiModelProperty("题目id")
    @TableField("teaching_question")
    private Long teachingQuestion;

    @ApiModelProperty("答卷记录表id")
    @TableField("record_id")
    private Long recordId;

    @ApiModelProperty("教师的作答内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("教师的作答结果")
    @TableField("result")
    private String result;

    @ApiModelProperty("教师该题目的得分")
    @TableField("score")
    private Double score;

    @ApiModelProperty("题目所属题库id")
    @TableField("teaching_question_bank_id")
    private Long teachingQuestionBankId;

    @ApiModelProperty("创建者")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作者")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;
}