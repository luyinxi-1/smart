package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "资料推送批量更新章节ID参数")
public class PushMaterialBatchUpdateCatalogParam {
    @ApiModelProperty(value = "资料推送ID列表", required = true)
    private Long id;

    @ApiModelProperty(value = "资料推送关联ID（章节ID）", required = true)
    private Long textbookCatalogId;
    @ApiModelProperty(value = "资料推送关联章节名称", required = true)
    private String textbookCatalogName;
    @ApiModelProperty(value = "教材目录UUID（章节UUID）")
    private String textbookCatalogUuid;
}
