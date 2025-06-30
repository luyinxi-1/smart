package com.upc.modular.auth.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/6/30 20:25
 */
@Data
public class SysAuthoritySearchParam extends PageBaseSearchParam {

    @ApiModelProperty("允许访问地址")
    private String accessUrl;
}
