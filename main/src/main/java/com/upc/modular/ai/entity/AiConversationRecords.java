package com.upc.modular.ai.entity;

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
 * @since 2025-07-09
 */
@Data
@Accessors(chain = true)
@TableName("ai_conversation_records")
@ApiModel(value = "AiConversationRecords对象", description = "")
public class AiConversationRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("所属对话ID")
    @TableField("ai_conversation_id")
    private Long aiConversationId;

    @ApiModelProperty("对话内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("对话身份")
    @TableField("role")
    private String role;

    @ApiModelProperty("联网搜索列表")
    @TableField("search")
    private String search;

    @ApiModelProperty("深度思考内容")
    @TableField("content_think")
    private String contentThink;

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
