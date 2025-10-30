package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "题目数量统计返回参数", description = "按题型统计题目数量的返回参数")
public class QuestionCountByTypeReturnParam {
    
    @ApiModelProperty(value = "题型ID", example = "1")
    private Integer typeId;
    
    @ApiModelProperty(value = "题型名称", example = "单选题")
    private String typeName;
    
    @ApiModelProperty(value = "题目数量", example = "10")
    private Long count;
}