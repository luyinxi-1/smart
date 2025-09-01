package com.upc.modular.textbook.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel(value = "教材审核分页查询参数")
public class TextbookReviewPageParam extends PageBaseSearchParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;
}
