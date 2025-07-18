package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TextbookNumberReturnParam {

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("数量")
    private Integer number;

}
