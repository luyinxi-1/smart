package com.upc.modular.auth.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class UploadBase64Param {
    @ApiModelProperty("Base64数据")
    private String base64Data;
}
