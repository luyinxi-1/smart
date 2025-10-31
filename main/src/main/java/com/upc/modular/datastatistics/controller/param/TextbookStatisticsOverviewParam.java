package com.upc.modular.datastatistics.controller.param;

import com.alibaba.excel.annotation.ExcelProperty;
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

    @ExcelProperty("教材ID")
    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ExcelProperty("教材名称")
    @ApiModelProperty("教材名称")
    private String textbookName;

    @ExcelProperty("阅读人数统计")
    @ApiModelProperty("阅读人数统计")
    private Long readerCount;

    @ExcelProperty("教学活动统计")
    @ApiModelProperty("教学活动统计")
    private Long teachingActivityCount;

    @ExcelProperty("素材数量")
    @ApiModelProperty("素材数量")
    private Long materialCount;

    @ExcelProperty("交流反馈数量")
    @ApiModelProperty("交流反馈数量")
    private Long communicationFeedbackCount;

    @ExcelProperty("教学思政数量")
    @ApiModelProperty("教学思政数量")
    private Long ideologicalMaterialCount;

    @ExcelProperty("答题正确率")
    @ApiModelProperty("答题正确率")
    private Double questionCorrectRate;

    @ExcelProperty("交流反馈参与量")
    @ApiModelProperty("交流反馈参与量")
    private Long communicationParticipationCount;

    @ExcelProperty("知识点批注量")
    @ApiModelProperty("知识点批注量")
    private Long annotationCount;
}