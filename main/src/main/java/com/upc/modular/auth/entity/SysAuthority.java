package com.upc.modular.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Data
@Accessors(chain = true)
@TableName("sys_authority")
@ApiModel(value = "SysAuthority对象", description = "")
public class SysAuthority implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("权限码")
    @TableField("auth_code")
    private String authCode;

    @ApiModelProperty("权限名称")
    @TableField("auth_name")
    private String authName;

    @ApiModelProperty("权限模块表id")
    @TableField("auth_model_id")
    private Long authModelId;

    @ApiModelProperty("权限模块名称")
    @TableField("auth_model_name")
    private String authModelName;

    @ApiModelProperty("顺序")
    @TableField("seq")
    private Integer seq;

    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("权限类型（0：菜单，1：按件，2：其他）")
    @TableField("auth_type")
    private Integer authType;

    @ApiModelProperty("路由")
    @TableField("url")
    private String url;

    @ApiModelProperty("可达路径")
    @TableField("access_url")
    private String accessUrl;

    @ApiModelProperty("创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private String creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private String operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
