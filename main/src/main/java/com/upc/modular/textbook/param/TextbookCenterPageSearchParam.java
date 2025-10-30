package com.upc.modular.textbook.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TextbookCenterPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("活动类型")
    private Integer activityType;
}
