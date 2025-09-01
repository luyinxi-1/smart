package com.upc.common.utils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UserInfoToRedis {

    @ApiModelProperty("用户ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("用户名（登录账号）")
    @TableField("username")
    private String username;

    @ApiModelProperty("密码（加密存储）")
    @TableField("password")
    private String password;

    @ApiModelProperty("用户类型（0管理员、1学生、2教师）")
    @TableField("user_type")
    private Integer userType;

    @ApiModelProperty("学工号")
    @TableField("identity_id")
    private String identityId;

    @ApiModelProperty("身份证号")
    @TableField("idcard")
    private String idcard;

    @ApiModelProperty("姓名")
    @TableField("name")
    private String name;

    @ApiModelProperty("性别（0男、1女）")
    @TableField("gender")
    private String gender;

    @ApiModelProperty("学院")
    @TableField("college")
    private String college;

    @ApiModelProperty("出生日期")
    @TableField("birthday")
    private String birthday;

    @ApiModelProperty("电子邮件")
    @TableField("email")
    private String email;

    @ApiModelProperty("电话")
    @TableField("phone")
    private String phone;

    @ApiModelProperty("用户状态（如启用、禁用）")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("用户昵称")
    @TableField("nickname")
    private String nickname;

    @ApiModelProperty("学生或教师id")
    private Long schoolId;

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

}
