package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学生做题情况统计请求参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "StudentQuestionAnsweringRequestParam", description = "学生做题情况统计请求参数")
public class StudentQuestionAnsweringRequestParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("学生ID")
    private Long studentId;
}
