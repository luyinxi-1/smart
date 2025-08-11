package com.upc.modular.homepage.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HomePagePromotionListSearchParam {

    @ApiModelProperty("展示个数")
    private Integer listNumber = 5;

}
