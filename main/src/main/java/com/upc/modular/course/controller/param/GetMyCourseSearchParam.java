package com.upc.modular.course.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetMyCourseSearchParam {
    @ApiModelProperty("课程名称")
    private String courseName;
}
