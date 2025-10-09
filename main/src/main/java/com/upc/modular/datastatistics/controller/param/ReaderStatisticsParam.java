package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 阅读人员统计参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "ReaderStatisticsParam", description = "阅读人员统计参数")
public class ReaderStatisticsParam {

    @ApiModelProperty("学生ID")
    private Long studentId;

    @ApiModelProperty("学生姓名")
    private String studentName;

    @ApiModelProperty("阅读时长(分钟)")
    private Long readingDuration;

    @ApiModelProperty("学习行为等级(高/中/低)")
    private String learningBehavior;

    @ApiModelProperty("阅读章节数")
    private Integer chapterCount;

    @ApiModelProperty("最后阅读时间")
    private String lastReadingTime;

    @ApiModelProperty("学习进度百分比")
    private Double progressPercentage;
}
