package com.upc.modular.teachingActivities.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DiscussionTopicReplyPageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("话题id")
    private Long topicId;

    @ApiModelProperty("排序方式(0：按时间倒序 1：按点赞数)")
    private Integer order = 0;

}
