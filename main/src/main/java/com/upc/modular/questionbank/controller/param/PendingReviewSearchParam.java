package com.upc.modular.questionbank.controller.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PendingReviewSearchParam {

    @ApiModelProperty(value = "题库ID", required = true)
    private Long bankId;

}
