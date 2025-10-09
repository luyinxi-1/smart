package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 做题情况统计参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "QuestionAnsweringStatisticsParam", description = "做题情况统计参数")
public class QuestionAnsweringStatisticsParam {

    @ApiModelProperty("章节ID")
    private Long chapterId;

    @ApiModelProperty("章节名称")
    private String chapterName;

    @ApiModelProperty("章节掌握程度(百分比)")
    private Double masteryLevel;

    @ApiModelProperty("答题总数")
    private Integer totalQuestions;

    @ApiModelProperty("正确答题数")
    private Integer correctAnswers;

    @ApiModelProperty("正确率")
    private Double correctRate;

    @ApiModelProperty("参与学生数")
    private Integer participantCount;

    @ApiModelProperty("平均得分")
    private Double averageScore;

    @ApiModelProperty("章节层级")
    private Integer chapterLevel;

    @ApiModelProperty("父章节ID")
    private Long parentChapterId;
}
