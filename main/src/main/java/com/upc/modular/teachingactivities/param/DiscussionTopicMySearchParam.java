package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DiscussionTopicMySearchParam {

    @ApiModelProperty("回复的id")
    private Long replyId;

    @ApiModelProperty("活动标题")
    private String topicTitle;

}
