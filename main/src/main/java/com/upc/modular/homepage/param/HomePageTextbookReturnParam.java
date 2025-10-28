package com.upc.modular.homepage.param;

import com.upc.modular.homepage.entity.HomePageTextbook;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HomePageTextbookReturnParam extends HomePageTextbook {
    @ApiModelProperty("作者姓名")
    private String authorName;

    @ApiModelProperty("修改者姓名")
    private String operatorName;

    @ApiModelProperty("教材信息")
    private TextbookPageReturnParam textbook;
}
