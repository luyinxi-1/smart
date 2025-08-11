package com.upc.modular.auth.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@ApiModel("权限信息")
public class AuthParam {

    @ApiModelProperty("id(新增时不填写)")
    private Long id;

    @ApiModelProperty("权限模块表id")
    @NotNull(message = "权限模块参数不能为空")
    private Long authModelId;

    @ApiModelProperty("权限模块名称")
    private String authModelName;

    @ApiModelProperty("权限名称")
    @NotEmpty(message = "权限名称参数不能为空")
    private String authName;

    @ApiModelProperty("顺序")
    @NotNull(message = "顺序参数不能为空")
    private Integer seq;

    @ApiModelProperty("状态")
    @NotNull(message = "状态参数不能为空")
    private Integer status;

    @ApiModelProperty("权限类型（0：菜单，1：按件，2：其他）")
    @NotNull(message = "权限类型参数不能为空")
    private Integer authType;

    @ApiModelProperty("路由")
    @NotEmpty(message = "路由参数不能为空")
    private String url;

    @ApiModelProperty("可达路径")
    private String accessUrl;

}
