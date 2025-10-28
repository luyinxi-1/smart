package com.upc.modular.questionbank.controller.param;

import com.upc.modular.questionbank.entity.TeachingQuestionClassification;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TeachingQuestionClassificationReturnVo", description = "题目分类返回VO")
public class TeachingQuestionClassificationReturnVo extends TeachingQuestionClassification {

    @ApiModelProperty("创建人姓名")
    private String creatorName;

    @ApiModelProperty("子分类列表")
    private List<TeachingQuestionClassificationReturnVo> childrenVo;
}