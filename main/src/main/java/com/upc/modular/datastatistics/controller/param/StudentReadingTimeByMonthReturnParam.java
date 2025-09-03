package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentReadingTimeByMonthReturnParam", description = "学生月度阅读时长返回参数")
public class StudentReadingTimeByMonthReturnParam {

    @ApiModelProperty("月份")
    private Integer month;

    @ApiModelProperty("阅读时长")
    private Long readingTime;
}