package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class QuestionsBanksListPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("题目id")
    private Long questionId;

    @ApiModelProperty("题库id")
    private Long bankId;

    @ApiModelProperty("顺序")
    private Integer sequence;

    @ApiModelProperty("每道题目分值")
    private Double score;
}
