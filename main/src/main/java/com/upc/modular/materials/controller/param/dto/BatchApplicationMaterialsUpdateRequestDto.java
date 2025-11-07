package com.upc.modular.materials.controller.param.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
@ApiModel("批量更新应用素材绑定请求参数")

public class BatchApplicationMaterialsUpdateRequestDto {

    @ApiModelProperty("applicationMaterialsTextbookMappingList: 应用素材教材绑定关系列表")
    @Valid
    private List<ApplicationMaterialsTextbookMappingDto> applicationMaterialsTextbookMappingList;

    @ApiModelProperty("catalogList: 章节ID列表（可选，用于删除这些章节下的所有应用素材绑定）")
    private List<Long> catalogList;
}

