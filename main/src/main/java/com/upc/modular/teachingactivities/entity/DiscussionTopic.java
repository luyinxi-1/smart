package com.upc.modular.teachingactivities.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@Data
@Accessors(chain = true)
@TableName("discussion_topic")
@ApiModel(value = "DiscussionTopic对象", description = "")
public class DiscussionTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("教学活动主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("话题的标题")
    @TableField("topic_title")
    private String topicTitle;

    @ApiModelProperty("话题的内容")
    @TableField("topic_content")
    private String topicContent;

    @ApiModelProperty("话题的类型")
    @TableField("type")
    private String type;

    @ApiModelProperty("留言的类型")
    @TableField("message_type")
    private Integer messageType;

    @ApiModelProperty("讨论关联的教材主表")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("讨论关联的教材目录")
    @TableField("textbook_catalog_id")
    private Long textbookCatalogId;

    @ApiModelProperty("话题创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("话题创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("话题操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("话题操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
