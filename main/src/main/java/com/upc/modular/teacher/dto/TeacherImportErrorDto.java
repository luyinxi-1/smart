package com.upc.modular.teacher.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeacherImportErrorDto extends TeacherImportDto{

    @ApiModelProperty("错误原因")
    private String errorReason;

}
