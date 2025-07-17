package com.upc.modular.course.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CourseTextbookListSearchParam {

    @ApiModelProperty("课程id")
    private Long courseId;

}
