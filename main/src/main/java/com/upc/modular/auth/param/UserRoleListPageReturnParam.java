package com.upc.modular.auth.param;

import com.upc.modular.auth.entity.UserRoleList;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class UserRoleListPageReturnParam extends UserRoleList {

    @ApiModelProperty("角色名")
    private String roleName;

    @ApiModelProperty("用户名")
    private String username;
}
