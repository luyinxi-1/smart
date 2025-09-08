package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.IdeologicalMaterial;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/9/8 10:59
 */
@Data
public class IdeologicalMaterialInsertAndUpdateParam extends IdeologicalMaterial {

    @ApiModelProperty("地址前缀")
    private String addressPrefix;

}
