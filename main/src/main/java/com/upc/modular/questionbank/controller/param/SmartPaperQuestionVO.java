package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 智能组卷返回的题目信息
 *
 * @author system
 * @since 2025-10-28
 */
@Data
@ApiModel(value = "智能组卷题目信息", description = "智能组卷返回的题目信息")
public class SmartPaperQuestionVO {

    @ApiModelProperty(value = "题目ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "题型（1-单选，2-多选，3-判断，4-填空，5-简答等）", example = "1")
    private Integer type;

    @ApiModelProperty(value = "题目名称/内容", example = "以下哪个选项是正确的？")
    private String content;

    @ApiModelProperty(value = "难度等级（1-简单，2-中等，3-困难）", example = "2")
    private Integer difficulty;
}


