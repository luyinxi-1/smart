package com.upc.modular.teachingActivities.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DiscussionTopicMyPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("话题的标题")
    private LocalDateTime startTime;

    @ApiModelProperty("话题的类型")
    private LocalDateTime endTime;

}
