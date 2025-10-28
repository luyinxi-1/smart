package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("通用分页请求参数")
public class PageParam {
    @ApiModelProperty(value = "当前页码", example = "1")
    private long current = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private long size = 10;
}
    