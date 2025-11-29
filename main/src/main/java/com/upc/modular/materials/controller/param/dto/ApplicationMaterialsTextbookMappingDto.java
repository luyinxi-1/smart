package com.upc.modular.materials.controller.param.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ApplicationMaterialsTextbookMappingDto {

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("应用素材id（可选）")
    private Long applicationMaterialId;

    @ApiModelProperty("章节名称（可选）")
    private String textbookCatalogName;

    @ApiModelProperty("章节ID（可选）")
    private Long textbookCatalogId;

    @ApiModelProperty("备用章节ID")
    private Long textbookCatalogId2;

    @ApiModelProperty("章节UUID")
    private String textbookCatalogUuId;
}