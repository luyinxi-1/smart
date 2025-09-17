package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MyJoinDiscussionTopicSearchParam {

    @ApiModelProperty("教材分类id")
    private Long classificationId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("身份ID（学号或工号）")
    private String identityId;


}
