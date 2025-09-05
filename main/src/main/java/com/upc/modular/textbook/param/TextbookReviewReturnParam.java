package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.TextbookReview;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "教材审核记录返回参数")
public class TextbookReviewReturnParam extends TextbookReview {

    @ApiModelProperty("创建人姓名")
    private String creatorName;

    @ApiModelProperty("操作人姓名")
    private String operatorName;
}
