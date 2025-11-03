package com.upc.modular.materials.controller.param.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ApplicationMaterialsTextbookMappingDto {

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("应用素材id")
    private Long applicationMaterialId;

    @ApiModelProperty("章节名称")
    private String chapterName;

    @ApiModelProperty("章节ID")
    private Long chapterId;

    @ApiModelProperty("章节UUID")
    private String chapterUuid;
}

