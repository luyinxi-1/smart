package com.upc.modular.discussion.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @since 2025-07-07
 */
@Data
@Accessors(chain = true)
@TableName("discussion_topic_reply")
@ApiModel(value = "DiscussionTopicReply对象", description = "")
public class DiscussionTopicReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("回复表主键")
    @TableId("id")
    private Long id;

    @ApiModelProperty("回复的内容")
    @TableField("reply_content")
    private String replyContent;

    @ApiModelProperty("关联教学活动的类型")
    @TableField("type")
    private Integer type;

    @ApiModelProperty("回复关联的教学活动ID")
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
