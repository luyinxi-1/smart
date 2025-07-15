package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
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
@Data
@Accessors(chain = true)
@TableName("student_exercises_record")
@ApiModel(value = "StudentExercisesRecord对象", description = "")
public class StudentExercisesRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("题库id")
    @TableField("teaching_question_bank_id")
    private Long teachingQuestionBankId;

    @ApiModelProperty("学生id")
    @TableField("student_id")
    private Long studentId;

    @ApiModelProperty("学生作答次数")
    @TableField("exercise_num")
    private Integer exerciseNum;

    @ApiModelProperty("学生在该题库的作答成绩")
    @TableField("score")
    private Double score;

    @ApiModelProperty("答卷状态(0：答题中；1：待批改；2：已完成 (所有题目都已评分，总分已计算))")
    @TableField("status")
    private Integer status;

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
