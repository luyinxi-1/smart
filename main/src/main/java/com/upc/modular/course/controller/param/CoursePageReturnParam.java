package com.upc.modular.course.controller.param;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CoursePageReturnParam {
    @ApiModelProperty("课程ID")
    private Long id;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("课程学分")
    private Integer credit;

    @ApiModelProperty("课程描述")
    private String description;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("用户id")
    private Long userId;

}