package com.upc.modular.auth.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ImportSysUserReturnParam {
    @ApiModelProperty(value = "新增数量")
    private long insertTotal;

    @ApiModelProperty(value = "更新数量")
    private long updateTotal;
}
