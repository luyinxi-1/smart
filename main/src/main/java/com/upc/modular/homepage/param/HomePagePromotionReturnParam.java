package com.upc.modular.homepage.param;

import com.upc.modular.homepage.entity.HomePagePromotion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.annotations.ApiModelProperty;
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HomePagePromotionReturnParam extends HomePagePromotion {

    @ApiModelProperty("作者姓名")
    private String authorName;

    @ApiModelProperty("修改者姓名")
    private String operatorName;
}
