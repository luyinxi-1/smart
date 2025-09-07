package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "题目题库批量关联参数")
public class QuestionsBanksListBatchParam {

    @ApiModelProperty("题库ID")
    private Long bankId;

    @ApiModelProperty("题目题库关联ID（更新时使用）")
    private Long id;

    @ApiModelProperty("题目列表")
    private List<QuestionScoreParam> list;

    @Data
    @ApiModel(value = "题目分数参数")
    public static class QuestionScoreParam {
        @ApiModelProperty("题目题库关联ID（更新时使用")
        private Long id;

        @ApiModelProperty("题目ID")
        private Long questionId;

        @ApiModelProperty("分数")
        private Double score;
        @ApiModelProperty("顺序")
        private Integer sequence;
    }
}

