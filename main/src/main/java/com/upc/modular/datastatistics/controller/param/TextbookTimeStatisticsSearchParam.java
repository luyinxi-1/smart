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

    @ApiModelProperty("时间查询方式（1 按年 2 按月 3 按日）")
    private Integer queryMethod = 1;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;
} 