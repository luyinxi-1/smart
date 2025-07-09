package com.upc.modular.student.entity;

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
@TableName("student")
@ApiModel(value = "Student对象", description = "")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("用户学号")
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

    @ApiModelProperty("学院")
    @TableField("college")
    private String college;

    @ApiModelProperty("生日")
    @TableField("birthday")
    private String birthday;

    @ApiModelProperty("电子邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty("电话")
    @TableField("phone")
    private String phone;

    @ApiModelProperty("班级id")
    @TableField("class_id")
    private Long classId;

    @ApiModelProperty("账号状态")
    @TableField("account_status")
    private Integer accountStatus;

    @ApiModelProperty("职务")
    @TableField("position")
    private String position;

    @ApiModelProperty("入学日期")
    @TableField("enrollment_data")
    private LocalDateTime enrollmentData;

    @ApiModelProperty("预计结业时间")
    @TableField("planned_graduation_date")
    private LocalDateTime plannedGraduationDate;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("证件照片")
    @TableField("id_photo")
    private String idPhoto;

    @ApiModelProperty("专业")
    @TableField("major")
    private String major;

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
