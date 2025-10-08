package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 教学思政批量更新章节ID参数
 */
@Data
@ApiModel(value = "教学思政批量更新章节ID参数")
public class IdeologicalMaterialBatchUpdateCatalogParam {

    @ApiModelProperty(value = "教学思政ID列表", required = true)
    private List<Long> ids;

    @ApiModelProperty(value = "教材目录ID（章节ID）", required = true)
    private Long textbookCatalogId;
}