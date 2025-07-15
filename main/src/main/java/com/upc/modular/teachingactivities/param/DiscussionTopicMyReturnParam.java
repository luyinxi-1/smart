package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class DiscussionTopicMyReturnParam {

    @ApiModelProperty("话题的标题")
    private String topicTitle;

    @ApiModelProperty("回复的内容")
    private String replyContent;

    @ApiModelProperty("回复发表者")
    private String replyAuthor;

    @ApiModelProperty("回复发表时间")
    private LocalDateTime replyDateTime;

    @ApiModelProperty("回复列表")
    private List<DiscussionTopicMyReplyList> replyList;

    @ApiModelProperty("是否为自身回复(0：不是，1：是)")
    private Integer isMine;

}
