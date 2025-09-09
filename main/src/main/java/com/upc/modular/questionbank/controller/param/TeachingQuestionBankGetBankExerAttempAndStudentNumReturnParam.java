package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeachingQuestionBankGetBankExerAttempAndStudentNumReturnParam {
    @ApiModelProperty("题库限制的答题次数 (-1 代表不限制)")
    private Integer maxAttempts;

    @ApiModelProperty("学生已经完成的答题次数")
    private Integer studentAttemptedNum;
}
