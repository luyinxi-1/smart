package com.upc.modular.auth.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserRoleListInsertParam {

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("角色id")
    private List<Long> roleId;

}
