package com.upc.modular.datastatistics.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 教师统计实体类
 * </p>
 *
 * @author cyy
 * @since 2025-09-02
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("teacher_statistics")
@ApiModel(value = "TeacherStatistics对象", description = "教师统计信息")
public class TeacherStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @ApiModelProperty("教师ID")
    @TableField("teacher_id")
    private Long teacherId;

    @ApiModelProperty("授课班级数量")
    @TableField("class_count")
    private Integer classCount;

    @ApiModelProperty("授课人数")
    @TableField("student_count")
    private Integer studentCount;

    @ApiModelProperty("教材数量")
    @TableField("textbook_count")
    private Integer textbookCount;

    @ApiModelProperty("授课课程数量")
    @TableField("course_count")
    private Integer courseCount;

    @ApiModelProperty("统计时间")
    @TableField("statistics_date")
    private LocalDateTime statisticsDate;

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
