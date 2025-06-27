package com.upc.modular.auth.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @Author: xth
 * @Date: 2025/6/27 20:54
 */
@Data
@ApiModel("用户登录账号密码")
public class UserLoginParam {

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("密码")
    private String password;

}