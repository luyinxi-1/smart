package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学生做题情况统计参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "StudentQuestionAnsweringStatisticsParam", description = "学生做题情况统计参数")
public class StudentQuestionAnsweringStatisticsParam {

    @ApiModelProperty("章节ID")
    private Long chapterId;

    @ApiModelProperty("章节名称")
    private String chapterName;

    @ApiModelProperty("学生章节做题时长(分钟)")
    private Long questionAnsweringDuration;

    @ApiModelProperty("学生章节得分")
    private Double chapterScore;

    @ApiModelProperty("学生章节正确率")
    private Double correctRate;

    @ApiModelProperty("学生答题总数")
    private Integer totalQuestions;

    @ApiModelProperty("学生正确答题数")
    private Integer correctAnswers;

    @ApiModelProperty("学生平均得分")
    private Double averageScore;

    @ApiModelProperty("章节层级")
    private Integer chapterLevel;

    @ApiModelProperty("父章节ID")
    private Long parentChapterId;

    @ApiModelProperty("学生章节阅读时长(分钟)")
    private Long readingDuration;

    @ApiModelProperty("学生章节掌握程度百分比")
    private Double masteryPercentage;
}
