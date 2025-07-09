package com.upc.modular.student.controller.param;

import com.upc.modular.teacher.dto.TeacherImportDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
@Data
@Accessors(chain = true)
public class StudentImportErrorDto {

        @ApiModelProperty("错误原因")
        private String errorReason;


}
