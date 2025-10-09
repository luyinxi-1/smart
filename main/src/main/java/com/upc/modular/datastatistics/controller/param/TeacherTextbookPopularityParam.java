package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教师教材热度参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "TeacherTextbookPopularityParam", description = "教师教材热度参数")
public class TeacherTextbookPopularityParam {

    @ApiModelProperty("排名")
    private Integer rank;

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("教材热度分数")
    private Integer popularityScore;

    @ApiModelProperty("阅读人数")
    private Long readerCount;

    @ApiModelProperty("阅读时长(分钟)")
    private Long readingDurationMinutes;

    @ApiModelProperty("教学活动数量")
    private Long teachingActivityCount;

    @ApiModelProperty("交流反馈数量")
    private Long communicationFeedbackCount;
}
