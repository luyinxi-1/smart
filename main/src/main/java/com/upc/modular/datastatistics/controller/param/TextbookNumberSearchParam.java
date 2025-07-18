package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TextbookNumberSearchParam {

    @ApiModelProperty("时间查询方式（0 全查 1 按年 2 按月）")
    private Integer queryMethod = 0;
}
