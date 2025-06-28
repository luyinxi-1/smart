package com.upc.modular.auth.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/6/28 10:25
 */
@Data
public class SysRoleSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("状态")
    private Integer status;
}
