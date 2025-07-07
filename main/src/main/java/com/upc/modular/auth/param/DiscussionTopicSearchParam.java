package com.upc.modular.auth.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/7 19:49
 */
@Data
public class DiscussionTopicSearchParam {

    @ApiModelProperty("话题的标题")
    private String topicTitle;

    @ApiModelProperty("话题的类型")
    private Integer type;

    @ApiModelProperty("讨论关联的教材主表")
    private Long textbookId;

    @ApiModelProperty("讨论关联的教材目录")
    private Long textbookCatalogId;
}
