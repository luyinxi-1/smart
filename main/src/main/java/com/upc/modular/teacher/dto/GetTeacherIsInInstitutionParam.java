package com.upc.modular.teacher.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetTeacherIsInInstitutionParam {

    @ApiModelProperty("机构id")
    private Long institutionId;

    @ApiModelProperty("教师id")
    private Long teacherId;
}
