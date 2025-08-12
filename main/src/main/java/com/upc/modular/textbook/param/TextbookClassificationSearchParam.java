package com.upc.modular.textbook.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TextbookClassificationSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("分类名称")
    private String classificationName;
}
