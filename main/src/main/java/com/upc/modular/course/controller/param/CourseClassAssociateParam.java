package com.upc.modular.course.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CourseClassAssociateParam {
    @ApiModelProperty("课程id")
    private Long courseId;

    @ApiModelProperty("要关联的班级id列表")
    private List<Long> classIdList;
}
