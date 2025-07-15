package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DiscussionTopicMyReplyList {

    @ApiModelProperty("回复主键")
    private Long id;

    @ApiModelProperty("回复的内容")
    private String replyContent;

    @ApiModelProperty("回复创建人")
    private String creatorName;

    @ApiModelProperty("回复时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("点赞数")
    private Integer likeNumber;

}
