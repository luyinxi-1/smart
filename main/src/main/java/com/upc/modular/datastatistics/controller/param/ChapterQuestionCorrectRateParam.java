package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 各章节习题正确率统计返回参数
 */
@Data
@Accessors(chain = true)
public class ChapterQuestionCorrectRateParam {

    @ApiModelProperty("章节ID")
    private Long chapterId;

    @ApiModelProperty("章节名称")
    private String chapterName;

    @ApiModelProperty("章节级别")
    private Integer chapterLevel;

    @ApiModelProperty("父章节ID")
    private Long parentChapterId;

    @ApiModelProperty("题库数量")
    private Long questionBankCount;

    @ApiModelProperty("总作答次数")
    private Long totalAnswerCount;

    @ApiModelProperty("正确作答次数")
    private Long correctAnswerCount;

    @ApiModelProperty("正确率")
    private Double correctRate;

    @ApiModelProperty("章节排序")
    private Integer sort;
}