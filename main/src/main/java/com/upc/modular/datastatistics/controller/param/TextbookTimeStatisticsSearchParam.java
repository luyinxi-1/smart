package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教材时间统计搜索参数
 */
@Data
@Accessors(chain = true)
public class TextbookTimeStatisticsSearchParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("时间范围筛选（week-本周，month-本月，year-本年）")
    private String timeRange;
} 