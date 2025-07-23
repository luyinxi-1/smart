package com.upc.modular.auth.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author xth
 * @since 2025-07-19
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_authority_model")
@ApiModel(value = "SysAuthorityModel对象", description = "")
public class SysAuthorityModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("权限模块码")
    @TableField("auth_model_code")
    private String authModelCode;

    @ApiModelProperty("权限模块名称")
    @TableField("auth_model_name")
    private String authModelName;

    @ApiModelProperty("上级模块id")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty("顺序")
    @TableField("seq")
    private Integer seq;

    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("是否外链")
    @TableField("have_url")
    private Integer haveUrl;

    @ApiModelProperty("外链")
    @TableField("url")
    private String url;

    @ApiModelProperty("图片链接")
    @TableField("pic_url")
    private String picUrl;

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
