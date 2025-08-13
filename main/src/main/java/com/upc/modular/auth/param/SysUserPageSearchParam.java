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

    @ApiModelProperty("用户昵称")
    private String nickname;
}
