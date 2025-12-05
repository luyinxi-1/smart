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
 * 教师练习记录表
 * </p>
 *
 * @author [你的名字]
 * @since [当前日期]
 */
@Data
@Accessors(chain = true)
@TableName("teacher_exercises_record")
@ApiModel(value = "TeacherExercisesRecord对象", description = "教师练习记录表")
public class TeacherExercisesRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("题库id")
    @TableField("teaching_question_bank_id")
    private Long teachingQuestionBankId;

    @ApiModelProperty("教师id")
    @TableField("teacher_id")
    private Long teacherId;

    @ApiModelProperty("练习数量")
    @TableField("exercise_num")
    private Integer exerciseNum;

    @ApiModelProperty("得分")
    @TableField("score")
    private Double score;

    @ApiModelProperty("创建者")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作者")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;  // 按你要求改为 Long 类型

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;  // 按你要求改为 LocalDateTime

    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status;

}