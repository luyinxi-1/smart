package com.upc.modular.institution.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetStudentParam {

    @ApiModelProperty("机构id")
    private Long institutionId;

    @ApiModelProperty("学生id")
    private Long studentId;
}
