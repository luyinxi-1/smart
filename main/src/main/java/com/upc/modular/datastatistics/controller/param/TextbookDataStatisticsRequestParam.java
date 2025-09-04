package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教材数据统计请求参数
 */
@Data
@Accessors(chain = true)
public class TextbookDataStatisticsRequestParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;

}