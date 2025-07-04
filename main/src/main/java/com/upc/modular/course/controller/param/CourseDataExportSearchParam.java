package com.upc.modular.course.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CourseDataExportSearchParam {
    @ApiModelProperty("课程id")
    private Long id;
}
