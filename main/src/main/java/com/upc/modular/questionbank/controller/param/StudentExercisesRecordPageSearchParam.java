package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StudentExercisesRecordPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("题库id")
    private Long teachingQuestionBankId;

    @ApiModelProperty("学生id")
    private Long studentId;

    @ApiModelProperty("学生作答次数")
    private Integer exerciseNum;

    @ApiModelProperty("学生在该题库的作答成绩")
    private Double score;

    @ApiModelProperty("答卷状态(0：答题中；1：待批改；2：已完成 (所有题目都已评分，总分已计算))")
    private Integer status;
}
