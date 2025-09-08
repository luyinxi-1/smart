package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import com.upc.common.responseparam.PageBaseReturnParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeachingQuestionPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("题型")
    private Integer type;

    @ApiModelProperty("题目内容")
    private String content;

    @ApiModelProperty("题目名称（用于模糊搜索）")
    private String questionName;

    @ApiModelProperty("难度等级")
    private Integer difficulty;

    @ApiModelProperty("状态（0禁用，1启用）")
    private Integer status;

    @ApiModelProperty("所属学科")
    private String subject;

    @ApiModelProperty("题目分类ID（查询该分类及子级）")
    private Long classificationId;

}
