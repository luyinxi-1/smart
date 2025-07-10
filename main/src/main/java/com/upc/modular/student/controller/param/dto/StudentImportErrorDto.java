package com.upc.modular.student.controller.param.dto;

import io.swagger.annotations.ApiModelProperty;

public class StudentImportErrorDto extends StudentImportDto{

    @ApiModelProperty("错误原因")
    private String errorReason;

}
