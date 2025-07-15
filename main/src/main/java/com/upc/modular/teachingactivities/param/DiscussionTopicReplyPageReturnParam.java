package com.upc.modular.teachingactivities.param;

import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DiscussionTopicReplyPageReturnParam extends DiscussionTopicReply {

    @ApiModelProperty("点赞数")
    private Integer likeNumber;

    @ApiModelProperty("回复数")
    private Integer replyNumber;

    @ApiModelProperty("回复人姓名")
    private String creatorName;

    @ApiModelProperty("是否为自身回复(0：不是，1：是)")
    private Integer isMine;

}
