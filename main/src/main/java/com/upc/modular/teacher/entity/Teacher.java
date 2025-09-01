package com.upc.modular.teacher.entity;

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
 * @since 2025-07-01
 */
@Data
@Accessors(chain = true)
@TableName("teacher")
@ApiModel(value = "Teacher对象", description = "")
public class Teacher implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("工号")
    @TableField("identity_id")
    private String identityId;

    @ApiModelProperty("身份证号")
    @TableField("idcard")
    private String idcard;

    @ApiModelProperty("姓名")
    @TableField("name")
    private String name;

    @ApiModelProperty("性别")
    @TableField("gender")
    private String gender;

    @ApiModelProperty("民族")
    @TableField("nationality")
    private String nationality;

    @ApiModelProperty("生日")
    @TableField("birthday")
    private String birthday;

    @ApiModelProperty("职务")
    @TableField("position")
    private String position;

    @ApiModelProperty("职称")
    @TableField("professional_title")
    private String professionalTitle;

    @ApiModelProperty("邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty("电话")
    @TableField("phone")
    private String phone;

    @ApiModelProperty("介绍")
    @TableField("introduction")
    private String introduction;

    @ApiModelProperty("学历（0：本科，1：硕士，2：博士）")
    @TableField("educational_background")
    private Integer educationalBackground = 0;

    @ApiModelProperty("是否为党员（0为否，1为是）")
    @TableField("is_party_number")
    private Integer isPartyNumber = 0;

    @ApiModelProperty("教学年限")
    @TableField("teaching_years")
    private String teachingYears;

    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status = 1;

    @ApiModelProperty("创建者")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作者")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
