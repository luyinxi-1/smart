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
@TableName("learning_notes")
@ApiModel(value = "LearningNotes对象", description = "")
public class LearningNotes implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("学习笔记表主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("笔记内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("关联的教材ID")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("关联的目录ID")
    @TableField("catalogue_id")
    private Long catalogueId;

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
