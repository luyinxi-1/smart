package com.upc.modular.teachingactivities.param;

import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MyJoinDiscussionTopicDiscussionTopicReturnParam extends DiscussionTopic {

    @ApiModelProperty("创建者姓名")
    private String creatorName;

    @ApiModelProperty("创建者身份")
    private String creatorRole;

    @ApiModelProperty("回复数")
    private Integer replyCount;

    @ApiModelProperty("讨论关联的教材名称")
    private String textbookName;

    @ApiModelProperty("讨论关联的教材目录名称")
    private String textbookCatalogName;

}
