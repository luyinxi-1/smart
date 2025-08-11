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

    @ApiModelProperty("讨论关联的教材名称")
    private String textbookName;

    @ApiModelProperty("讨论关联的教材目录名称")
    private String textbookCatalogName;

    @ApiModelProperty("讨论下的回复数")
    private Integer replyCount;
}
