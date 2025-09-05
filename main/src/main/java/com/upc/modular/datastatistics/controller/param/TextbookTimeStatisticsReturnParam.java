package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教材时间统计返回参数
 */
@Data
@Accessors(chain = true)
public class TextbookTimeStatisticsReturnParam {

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("数量")
    private Long count;

    @ApiModelProperty("时长(分钟)")
    private Long duration;
} 