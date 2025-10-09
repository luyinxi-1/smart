package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教材数据统计概览参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "TextbookStatisticsOverviewParam", description = "教材数据统计概览参数")
public class TextbookStatisticsOverviewParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("阅读人数统计")
    private Long readerCount;

    @ApiModelProperty("教学活动统计")
    private Long teachingActivityCount;

    @ApiModelProperty("素材数量")
    private Long materialCount;

    @ApiModelProperty("交流反馈数量")
    private Long communicationFeedbackCount;

    @ApiModelProperty("教学思政数量")
    private Long ideologicalMaterialCount;

    @ApiModelProperty("答题正确率")
    private Double questionCorrectRate;

    @ApiModelProperty("交流反馈参与量")
    private Long communicationParticipationCount;

    @ApiModelProperty("知识点批注量")
    private Long annotationCount;
}
