package com.upc.modular.materials.controller.param.vo;

import com.upc.modular.materials.entity.TeachingMaterials;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode
@ApiModel(value = "教学素材返回对象")
public class TeachingMaterialsReturnVo extends TeachingMaterials {
    @ApiModelProperty("机构名称")
    private String institutionName;

    @ApiModelProperty("机构id")
    private Long institutionId;
}
