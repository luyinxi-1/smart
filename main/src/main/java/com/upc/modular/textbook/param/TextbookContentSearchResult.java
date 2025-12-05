package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "TextbookContentSearchResult", description = "教材内容搜索结果")
public class TextbookContentSearchResult {
    @ApiModelProperty("匹配内容所在的完整路径（包含所有父级目录名称）")
    private String fullPath;

    @ApiModelProperty("二级目录ID（catalog_level为2的目录）")
    private Long CatalogId;

    @ApiModelProperty("匹配关键字的部分内容")
    private String matchedContent;
}