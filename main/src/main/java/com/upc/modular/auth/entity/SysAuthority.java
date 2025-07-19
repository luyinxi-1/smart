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

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("允许访问地址")
    @TableField("access_url")
    private String accessUrl;

    @ApiModelProperty("该权限的父节点，0表示根节点")
    @TableField("father_id")
    private Long fatherId;

    @ApiModelProperty("放行路径名")
    @TableField("access_name")
    private String accessName;

    @ApiModelProperty("权限组id（一般只有父级权限即father_id字段为0的才配置权限组）")
    @TableField("sys_authority_model_id")
    private Long sysAuthorityModelId;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
