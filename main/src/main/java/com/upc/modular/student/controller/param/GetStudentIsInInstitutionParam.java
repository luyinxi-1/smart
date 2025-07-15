package com.upc.modular.student.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetStudentIsInInstitutionParam {

    @ApiModelProperty("机构id")
    private Long institutionId;

    @ApiModelProperty("学生id")
    private Long StudentId;

}
