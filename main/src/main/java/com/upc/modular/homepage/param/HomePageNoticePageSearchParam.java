package com.upc.modular.homepage.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HomePageNoticePageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("公告类型")
    private Integer type;

}
