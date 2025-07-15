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
@TableName("discussion_topic_reply")
@ApiModel(value = "DiscussionTopicReply对象", description = "")
public class DiscussionTopicReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("回复表主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("回复的内容")
    @TableField("reply_content")
    private String replyContent;

    @ApiModelProperty("类型（1：回复的话题；2：回复的其他回复。）")
    @TableField("type")
    private Integer type;

    @ApiModelProperty("回复关联的教学活动ID或关联的其他回复的id")
    @TableField("topic_id")
    private Long topicId;

    @ApiModelProperty("回复创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("回复创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("回复操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("回复操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
