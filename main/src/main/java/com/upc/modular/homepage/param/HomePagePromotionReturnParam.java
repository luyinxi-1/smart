package com.upc.modular.homepage.param;

import com.upc.modular.homepage.entity.HomePagePromotion;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
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

    @ApiModelProperty("教材信息")
    private TextbookPageReturnParam textbook;
}
