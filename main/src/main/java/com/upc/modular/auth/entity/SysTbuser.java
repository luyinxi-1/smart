package com.upc.modular.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_tbuser")
@ApiModel(value = "SysUser对象", description = "")
@Data
public class SysTbuser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户表主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户名")
    @TableField("username")
    private String username;

    @ApiModelProperty("昵称")
    @TableField("nickname")
    private String nickname;

    @ApiModelProperty("密码")
    @TableField("password")
    private String password;

    @ApiModelProperty("用户类型（0管理员、1学生、2教师）")
    @TableField("user_type")
    private Integer userType;

    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("机构id")
    @TableField("institution_id")
    private Long institutionId;

    @ApiModelProperty("用户头像")
    @TableField("user_picture")
    private String userPicture;

    @ApiModelProperty("创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @ApiModelProperty("创建人姓名")
    @TableField(exist = false)
    private String creatorName;

    // 统一认证相关字段
    @ApiModelProperty("统一认证sub")
    @TableField("cas_sub")
    private String casSub;

    @ApiModelProperty("统一认证姓名")
    @TableField("cas_name")
    private String casName;


}