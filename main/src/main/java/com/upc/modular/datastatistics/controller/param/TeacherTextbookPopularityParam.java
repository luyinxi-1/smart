package com.upc.modular.datastatistics.controller.param;

import com.alibaba.excel.annotation.ExcelProperty;
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

    @ExcelProperty("排名")
    @ApiModelProperty("排名")
    private Integer rank;

    @ExcelProperty("教材ID")
    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ExcelProperty("教材名称")
    @ApiModelProperty("教材名称")
    private String textbookName;

    @ExcelProperty("教材热度分数")
    @ApiModelProperty("教材热度分数")
    private Integer popularityScore;

    @ExcelProperty("阅读人数")
    @ApiModelProperty("阅读人数")
    private Long readerCount;

    @ExcelProperty("阅读时长(分钟)")
    @ApiModelProperty("阅读时长(分钟)")
    private Long readingDurationMinutes;

    @ExcelProperty("教学活动数量")
    @ApiModelProperty("教学活动数量")
    private Long teachingActivityCount;

    @ExcelProperty("交流反馈数量")
    @ApiModelProperty("交流反馈数量")
    private Long communicationFeedbackCount;
}
