package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StudentQuestionAnsweringStatisticsParam {
    @ApiModelProperty("章节ID")
    private Long chapterId;

    @ApiModelProperty("章节名称")
    private String chapterName;

    @ApiModelProperty("学生章节做题时长(小时)")
    private Long questionAnsweringDuration;

    @ApiModelProperty("章节得分")
    private Double chapterScore;

    @ApiModelProperty("正确率")
    private Double correctRate;

    @ApiModelProperty("总题数")
    private Integer totalQuestions;

    @ApiModelProperty("正确题数")
    private Integer correctAnswers;

    @ApiModelProperty("平均分")
    private Double averageScore;

    @ApiModelProperty("章节层级")
    private Integer chapterLevel;

    @ApiModelProperty("父章节ID")
    private Long parentChapterId;

    @ApiModelProperty("学生章节阅读时长(小时)")
    private Long readingDuration;

    @ApiModelProperty("掌握程度百分比")
    private Double masteryPercentage;
}