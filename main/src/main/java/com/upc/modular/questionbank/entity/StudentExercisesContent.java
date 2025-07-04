package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("student_exercises_content")
@ApiModel(value = "StudentExercisesContent对象", description = "")
public class StudentExercisesContent implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId("id")
    private Long id;

    @ApiModelProperty("学生id")
    @TableField("student_id")
    private Long studentId;

    @ApiModelProperty("题目id")
    @TableField("teaching_question")
    private Long teachingQuestion;

    @ApiModelProperty("学生的作答内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("学生的作答结果")
    @TableField("result")
    private String result;

    @ApiModelProperty("学生该题目的得分")
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
