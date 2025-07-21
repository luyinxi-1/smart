package com.upc.modular.course.controller.param;

import com.upc.modular.course.entity.Course;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class GetMyCourseReturnParam extends Course {

    @ApiModelProperty("教师姓名")
    private String teacherName;

    @ApiModelProperty("授课班级")
    private List<ClassInfoReturnParam> groupList;

    @ApiModelProperty("教材数量")
    private Long textbookNumber;
}
