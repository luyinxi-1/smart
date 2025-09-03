package com.upc.modular.course.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.modular.course.entity.CourseTextbookList;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class CourseTextbookListReturnParam extends CourseTextbookList {

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("教材封面")
    private String textbookPicture;
}
