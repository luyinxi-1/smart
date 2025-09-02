package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教师统计返回参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "TeacherStatisticsReturnParam", description = "教师统计返回参数")
public class TeacherStatisticsReturnParam {

    @ApiModelProperty("教师ID")
    private Long teacherId;

    @ApiModelProperty("教师姓名")
    private String teacherName;

    @ApiModelProperty("授课班级数量")
    private Integer classCount;

    @ApiModelProperty("授课人数")
    private Integer studentCount;

    @ApiModelProperty("教材数量")
    private Integer textbookCount;

    @ApiModelProperty("授课课程数量")
    private Integer courseCount;

    @ApiModelProperty("统计时间")
    private String statisticsDate;
}
