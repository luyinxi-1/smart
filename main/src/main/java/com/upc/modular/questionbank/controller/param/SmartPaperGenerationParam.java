package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * 智能组卷请求参数
 *
 * @author system
 * @since 2025-10-28
 */
@Data
@ApiModel(value = "智能组卷请求参数", description = "教师端智能组卷功能的请求参数")
public class SmartPaperGenerationParam {

    @ApiModelProperty(value = "教材ID", required = true, example = "1")
    private Long textbookId;

    @ApiModelProperty(value = "章节ID", required = true, example = "1")
    private Long chapterId;

    @ApiModelProperty(value = "难易程度（1-简单，2-中等，3-困难）", required = true, example = "2")
    private Integer difficulty;

    @ApiModelProperty(value = "每个题型对应的数量。key为题型（1-单选，2-多选，3-判断，4-填空，5-简答等），value为数量", 
                      required = true, 
                      example = "{\"1\": 10, \"2\": 5, \"4\": 8}")
    private Map<Integer, Integer> questionTypeCount;
}


