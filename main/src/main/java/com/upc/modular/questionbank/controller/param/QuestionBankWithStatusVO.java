package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class QuestionBankWithStatusVO {

    @ApiModelProperty("题库ID")
    private Long bankId;

    @ApiModelProperty("题库名称")
    private String bankName;

    @ApiModelProperty("所在章节ID")
    private Long catalogId;

    @ApiModelProperty("所在章节名称")
    private String catalogName;

    @ApiModelProperty("待批改的答卷数量")
    private Long pendingReviewCount;
}
