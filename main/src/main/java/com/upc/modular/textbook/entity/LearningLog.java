package com.upc.modular.textbook.entity;

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
 * @since 2025-07-14
 */
@Data
@Accessors(chain = true)
@TableName("learning_log")
@ApiModel(value = "LearningLog对象", description = "")
public class LearningLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教材id")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("目录章节id")
    @TableField("catalogue_id")
    private Long catalogueId;

    @ApiModelProperty("数据发送类型")
    @TableField(value = "data_type")
    private int dataType;

    @ApiModelProperty("创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("添加时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @ApiModelProperty("客户端生成的记录唯一ID(防重复同步)")
    @TableField("client_uuid")
    private String clientUuid;
    
    @ApiModelProperty("同步状态: 0-未同步, 1-已同步")
    @TableField("sync_status")
    private Integer syncStatus;
}