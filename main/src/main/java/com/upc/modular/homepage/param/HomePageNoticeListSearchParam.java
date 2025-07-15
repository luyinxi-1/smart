package com.upc.modular.homepage.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HomePageNoticeListSearchParam {

    @ApiModelProperty("公告类型")
    private Integer type;

    @ApiModelProperty("展示个数")
    private Integer listNumber = 5;
}
