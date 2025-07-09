package com.upc.modular.teacher.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TeacherImportErrorDto extends TeacherImportDto{

    @ApiModelProperty("错误原因")
    private String errorReason;

}
