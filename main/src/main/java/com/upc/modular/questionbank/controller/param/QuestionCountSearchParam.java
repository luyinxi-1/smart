package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "题目数量查询参数", description = "查询题目数量的参数")
public class QuestionCountSearchParam {

    @ApiModelProperty(value = "教材ID", required = true, example = "1")
    private Long textbookId;

    @ApiModelProperty(value = "章节ID", required = true, example = "1")
    private Long chapterId;
}