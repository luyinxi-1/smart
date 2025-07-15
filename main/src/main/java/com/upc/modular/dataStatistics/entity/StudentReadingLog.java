package com.upc.modular.dataStatistics.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @author la
 * @since 2025-07-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("student_reading_log")
@ApiModel(value = "StudentReadingLog对象", description = "")
public class StudentReadingLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @ApiModelProperty("学生id")
    @TableField("student_id")
    private Long studentId;

    @ApiModelProperty("教材id")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("阅读的章节id")
    @TableField("textbook_catalog_id")
    private Long textbookCatalogId;

    @ApiModelProperty("阅读开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @ApiModelProperty("本次阅读时长(分钟)")
    @TableField("duration_minutes")
    private Integer durationMinutes;

    @ApiModelProperty("客户端生成的记录唯一ID(防重复同步)")
    @TableField("client_uuid")
    private String clientUuid;

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
