package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教材数据统计返回参数
 */
@Data
@Accessors(chain = true)
public class TextbookDataStatisticsParam {

    @ApiModelProperty("阅读人数统计")
    private Long readerCount;

    @ApiModelProperty("教学活动数量")
    private Long teachingActivityCount;

    @ApiModelProperty("素材数量")
    private Long materialCount;

    @ApiModelProperty("阅读时长统计(分钟)")
    private Long readingDurationMinutes;

    @ApiModelProperty("交流反馈数量")
    private Long communicationFeedbackCount;

    @ApiModelProperty("教学思政数量")
    private Long ideologicalMaterialCount;
}