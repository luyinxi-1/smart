package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 教学活动批量更新章节ID参数
 */
@Data
@ApiModel(value = "教学活动批量更新章节ID参数")
public class DiscussionTopicBatchUpdateCatalogParam {

    @ApiModelProperty(value = "教学活动ID列表", required = true)
    private List<Long> ids;

    @ApiModelProperty(value = "教材目录ID（章节ID）", required = true)
    private Long textbookCatalogId;
}