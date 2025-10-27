package com.upc.modular.auth.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户登录返回结果")
public class UserLoginResultParam {

    @ApiModelProperty("认证token")
    private String token;

    @ApiModelProperty("用户ID")
    private Long userId;
}