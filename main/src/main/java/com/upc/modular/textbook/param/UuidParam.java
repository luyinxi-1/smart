package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * UUID参数类
 */
@Data
@ApiModel("UUID参数")
public class UuidParam {
    @ApiModelProperty("UUID列表")
    private String[] uuidList;
}