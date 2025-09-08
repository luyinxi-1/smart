package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentBehaviorReturnParam", description = "学生学习行为分析")
public class StudentBehaviorReturnParam {
    @ApiModelProperty("评估结果")
    private String habitType;

    @ApiModelProperty("规律性分数")
    private double regularityScore;
}
