package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DiscussionTopicMyPageReturnParam {

    @ApiModelProperty("话题的标题")
    private String topicTitle;

    @ApiModelProperty("回复的内容")
    private String replyContent;

    @ApiModelProperty("回复id")
    private Long replyId;

    @ApiModelProperty("回复创建时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("点赞数")
    private Integer likeNumber;

    @ApiModelProperty("回复数")
    private Integer replyNumber;

    @ApiModelProperty("教材名称")
    private String textbookName;
}
