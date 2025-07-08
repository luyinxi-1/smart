package com.upc.modular.teachingActivities.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DiscussionTopicSecondReplyPageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("回复id")
    private Long replyId;

}
