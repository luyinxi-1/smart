package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 教材时间统计搜索参数
 */
@Data
@Accessors(chain = true)
public class TextbookTimeStatisticsSearchParam {

    @ApiModelProperty("教材ID")
    @NotNull(message = "教材ID不能为空")
    private Long textbookId;

    @ApiModelProperty("时间范围筛选（week-本周，month-本月，year-本年）")
    @NotNull(message = "时间范围不能为空")
    private String timeRange;
} 