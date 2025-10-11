package com.upc.modular.materials.controller.param.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MaterialsTextbookMappingDto {

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("素材id")
    private Long materialId;

    @ApiModelProperty("章节名称")
    private String chapterName;

    @ApiModelProperty("章节ID")
    private  Long chapterId;

    @ApiModelProperty("章节UUID")
    private String chapterUuid;
}
