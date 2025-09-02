package com.upc.modular.materials.controller.param.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AttachmentListReturnParam {

    @ApiModelProperty("附件id")
    private Long id;

    @ApiModelProperty("附件类型")
    private String type;

    @ApiModelProperty("附件路径")
    private String filePath;

}
