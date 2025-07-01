package com.upc.modular.teacher.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ImportTeacherReturnVo {
    @ApiModelProperty(value = "新增数量")
    private long insertTotal;

    @ApiModelProperty(value = "更新数量")
    private long updateTotal;
}
