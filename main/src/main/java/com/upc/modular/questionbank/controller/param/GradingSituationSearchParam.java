package com.upc.modular.questionbank.controller.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GradingSituationSearchParam extends PageBaseSearchParam {

    @ApiModelProperty(value = "题库ID", required = true)
    private Long bankId;

}
