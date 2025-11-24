package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "MaterialTypeCountReturnParam", description = "素材类型及数量返回参数")
public class MaterialTypeCountReturnParam {

    @ApiModelProperty("素材类型")
    private String type;

    @ApiModelProperty("素材数量")
    private Long num;
}