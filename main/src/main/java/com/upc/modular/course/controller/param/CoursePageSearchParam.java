package com.upc.modular.course.controller.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CoursePageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("教师姓名")
    private String teacherName;
}