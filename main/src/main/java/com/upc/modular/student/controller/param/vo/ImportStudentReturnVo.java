package com.upc.modular.student.controller.param.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ImportStudentReturnVo {
    @ApiModelProperty(value = "新增数量")
    private long insertTotal;

    @ApiModelProperty(value = "更新数量")
    private long updateTotal;

}
