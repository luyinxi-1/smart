package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AnswerDetailDTO {

    @ApiModelProperty("题目id")
    private Long teachingQuestionId;

    @ApiModelProperty("学生答案内容")
    private String studentAnswer;
}
