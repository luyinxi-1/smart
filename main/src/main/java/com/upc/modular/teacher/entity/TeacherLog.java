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
 * @since 2025-08-21
 */
@Data
@Accessors(chain = true)
@TableName("teacher_log")
@ApiModel(value = "TeacherLog对象", description = "")
public class TeacherLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教师ID")
    @TableField("teacher_id")
    private Long teacherId;

    @ApiModelProperty("用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("修改内容")
    @TableField("change_content")
    private String changeContent;

    @ApiModelProperty("操作路径")
    @TableField("operation_path")
    private String operationPath;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
