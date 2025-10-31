package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 我的教学活动返回参数（区分创建和回复）
 */
@Data
public class MyDiscussionTopicReturnParam extends DiscussionTopicReturnParam {

    @ApiModelProperty("是否是我创建的活动: 0-否, 1-是")
    private Integer isMyCreated = 0;
    
    @ApiModelProperty("是否是我回复过的活动: 0-否, 1-是")
    private Integer isMyReplied = 0;

}