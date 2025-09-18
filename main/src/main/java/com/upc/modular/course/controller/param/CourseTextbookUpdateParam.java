package com.upc.modular.course.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "CourseTextbookUpdateParam对象", description = "课程教材更新参数")
public class CourseTextbookUpdateParam {

    @ApiModelProperty("课程id")
    private Long courseId;

    @ApiModelProperty("教材id列表")
    private List<Long> textbookIds;
}
