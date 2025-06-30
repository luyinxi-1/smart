package com.upc.modular.auth.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

    @ApiModelProperty("权限有效期（小时）")
    @TableField("validity_period")
    private Long validityPeriod;

    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status;

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
    @TableField("operation_time")
    private LocalDateTime operationTime;


}
