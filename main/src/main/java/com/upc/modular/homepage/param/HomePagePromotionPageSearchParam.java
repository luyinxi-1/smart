package com.upc.modular.homepage.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HomePagePromotionPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("状态")
    private Integer status;
}
