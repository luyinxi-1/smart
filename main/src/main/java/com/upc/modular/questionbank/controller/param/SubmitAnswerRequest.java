package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SubmitAnswerRequest {

    @ApiModelProperty("题库id")
    private Long bankId;

    @ApiModelProperty("学生做的题库答案")
    private List<AnswerDetailDTO> answers;

}
