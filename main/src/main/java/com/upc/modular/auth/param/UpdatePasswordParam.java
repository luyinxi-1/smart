package com.upc.modular.auth.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/8/16 12:11
 */
@Data
public class UpdatePasswordParam {
    @ApiModelProperty("旧密码")
    private String oldPassword;

    @ApiModelProperty("新密码")
    private String newPassword;

    @ApiModelProperty("用户id（非必填）")
    private Long id;
}
