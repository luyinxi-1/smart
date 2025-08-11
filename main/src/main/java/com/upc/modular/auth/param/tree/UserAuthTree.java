package com.upc.modular.auth.param.tree;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author sxz
 */
@Data
@ApiModel("用户权限")
public class UserAuthTree {

    @ApiModelProperty("用户管辖区域idList")
    private List<Long> areaIdList;

    @ApiModelProperty("功能权限")
    private FunctionAuthNode functionAuthNode;

}
