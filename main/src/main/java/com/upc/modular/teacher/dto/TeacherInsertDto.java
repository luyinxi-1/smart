package com.upc.modular.teacher.dto;

import com.upc.modular.teacher.entity.Teacher;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TeacherInsertDto extends Teacher {

    @ApiModelProperty("机构id")
    private Long institutionId;

}
