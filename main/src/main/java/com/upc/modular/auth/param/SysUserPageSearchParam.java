package com.upc.modular.auth.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SysUserPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("用户类型")
    private Integer userType;

    @ApiModelProperty("综合查询的昵称（顶部搜索框）")
    private String nickname;

    @ApiModelProperty("用户名称（下面输入框）")
    private String username;

    @ApiModelProperty("登录账号（下面输入框），对应表字段 username")
    private String userCode;

    @ApiModelProperty("使用状态，对应表字段 status")
    private Integer status;
}
