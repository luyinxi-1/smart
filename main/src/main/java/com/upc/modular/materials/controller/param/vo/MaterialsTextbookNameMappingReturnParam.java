package com.upc.modular.materials.controller.param.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MaterialsTextbookNameMappingReturnParam {

    @ApiModelProperty("素材id")
    private Long materialId;

    @ApiModelProperty("素材名称")
    private String materialName;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("教材名")
    private String textbookName;
}
