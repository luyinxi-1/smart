package com.upc.modular.questionbank.controller.param;

import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
@ApiModel("批量更新题库请求参数")
public class BatchQuestionBankUpdateRequestDto {

    @ApiModelProperty("teachingQuestionBankList: 教材题库列表")
    @Valid
    private List<TeachingQuestionBank> teachingQuestionBankList;

    @ApiModelProperty("catalogList: 章节ID列表")
    private List<Long> catalogList;
}