package com.upc.modular.teachingactivities.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/8/11 20:10
 */
@Data
public class DiscussionTopicReturnParam {

    @ApiModelProperty("话题的id")
    private Long id;

    @ApiModelProperty("讨论关联的教材名称")
    private String textbookName;

    @ApiModelProperty("活动名称")
    private String activityName;

    @ApiModelProperty("活动类型")
    private String activityType;

    @ApiModelProperty("讨论关联的教材目录名称")
    private String textbookCatalogName;

    @ApiModelProperty("创建人")
    private String creatorName;

    @ApiModelProperty("创建日期")
    private String addDatetime;

    @ApiModelProperty("讨论下的回复数")
    private Integer replyCount;

}
