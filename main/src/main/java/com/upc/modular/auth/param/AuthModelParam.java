package com.upc.modular.auth.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@ApiModel("权限模块新增和更新参数")
public class AuthModelParam {

    @ApiModelProperty("新增时不写")
    private Long id;

    @ApiModelProperty("上级模块id")
    @NotNull(message = "上级模块id不能为空")
    private Long parentId;

    @ApiModelProperty("权限模块名称")
    private String authModelName;

    @ApiModelProperty("顺序")
    @NotNull(message = "顺序不能为空")
    private Integer seq;

    @ApiModelProperty("状态")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty("是否外链")
    @NotNull(message = "是否外链不能为空")
    private Integer haveUrl;

    @ApiModelProperty("外链")
    private String url;

    @ApiModelProperty("图片链接")
    private String picUrl;

}
