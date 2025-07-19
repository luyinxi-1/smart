package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GradingSituationReturnVO {

    @ApiModelProperty("学生ID")
    private Long studentId;

    @ApiModelProperty("班级")
    private String className;

    @ApiModelProperty("学号")
    private String identityId;

    @ApiModelProperty("学生姓名")
    private String studentName;

    @ApiModelProperty("最终分数")
    private Double finalScore;

    @ApiModelProperty("题库ID")
    private Long bankId;

    @ApiModelProperty("产生此分数的答卷记录ID")
    private Long recordId;

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;
}
