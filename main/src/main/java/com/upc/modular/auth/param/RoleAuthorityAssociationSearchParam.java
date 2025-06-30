package com.upc.modular.auth.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/6/30 21:36
 */
@Data
public class RoleAuthorityAssociationSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("角色id")
    private Long roleId;

}
