package com.upc.modular.group.entity;

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
@TableName("\"group\"") // <--- 关键修改在这里
@ApiModel(value = "Group对象", description = "")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("班级名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("年级")
    @TableField("grade")
    private Long grade;

    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("教师")
    @TableField("teacher_id")
    private Long teacherId;

    @ApiModelProperty("教师姓名")
    @TableField(exist = false)
    private String teacherName;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("组织")
    @TableField("institution_id")
    private Long institutionId;

    @ApiModelProperty("默认教室")
    @TableField("default_classroom")
    private String defaultClassroom;

    @ApiModelProperty("入学日期")
    @TableField("admission_date")
    private LocalDateTime admissionDate;

    @ApiModelProperty("毕业日期")
    @TableField("graduation_date")
    private LocalDateTime graduationDate;

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

    @ApiModelProperty("班级状态")
    @TableField(value = "class_status")
    private LocalDateTime classStatus;

}