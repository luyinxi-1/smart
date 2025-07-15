package com.upc.modular.homepage.param;

import com.upc.modular.homepage.entity.HomePageNotice;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HomePageNoticeReturnParam extends HomePageNotice {

    @ApiModelProperty("作者姓名")
    private Integer authorName;

}
