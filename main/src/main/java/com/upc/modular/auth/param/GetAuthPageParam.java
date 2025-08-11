package com.upc.modular.auth.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("权限分页")
public class GetAuthPageParam extends PageBaseSearchParam {
    @ApiModelProperty("权限模块表id")
    @TableField("auth_model_id")
    private Long authModelId;

    @ApiModelProperty("权限模块名称")
    @TableField("auth_model_name")
    private String authModelName;

    @ApiModelProperty("权限名称")
    private String authName;

    @ApiModelProperty("顺序")
    private Integer seq;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("权限类型（0：菜单，1：按件，2：其他）")
    private Integer authType;

    @ApiModelProperty("路由")
    private String url;

    @ApiModelProperty("可达路径")
    private String accessUrl;

}
