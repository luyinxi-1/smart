package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeachingQuestionBankGetBankExerAttempAndStudentNumSearchParam {
//    @ApiModelProperty("学生id")
//    private Long studentId;

    @ApiModelProperty("题库id")
    private Long bankId;
}
