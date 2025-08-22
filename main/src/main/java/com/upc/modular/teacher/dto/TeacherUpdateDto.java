package com.upc.modular.teacher.dto;

import com.upc.modular.teacher.entity.Teacher;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TeacherUpdateDto extends Teacher {

    @ApiModelProperty("机构id")
    private Long institutionId;

}
