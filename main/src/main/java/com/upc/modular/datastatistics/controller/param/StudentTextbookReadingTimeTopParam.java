package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentTextbookReadingTimeTopParam", description = "学生教材阅读时长Top10")
public class StudentTextbookReadingTimeTopParam {
    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("阅读时长(小时)")
    private Long readingTime;

    @ApiModelProperty("学习占比")
    private Long proportion;
}