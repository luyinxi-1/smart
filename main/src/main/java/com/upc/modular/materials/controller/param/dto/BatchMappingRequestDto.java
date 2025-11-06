package com.upc.modular.materials.controller.param.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
@ApiModel("批量绑定请求参数")
public class BatchMappingRequestDto {
    
    @ApiModelProperty("TextbookMaterialsList: 教材附件列表")
    @Valid
    private List<MaterialsTextbookMappingDto> TextbookMaterialsList;
    
    @ApiModelProperty("ChapterList: 章节ID列表")
    private List<Long> ChapterList;
}