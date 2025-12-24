package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentReadingRankParam", description = "学生阅读排行榜参数")
public class StudentReadingRankParam {

    @ApiModelProperty("学生ID")
    private Long studentId;

    @ApiModelProperty("学生姓名")
    private String studentName;

    @ApiModelProperty("班级ID")
    private Long groupId;

    @ApiModelProperty("班级名称")
    private String groupName;

    @ApiModelProperty("阅读时长(小时)")
    private double readingCount;

    @ApiModelProperty("排名")
    private Long rank;

    @ApiModelProperty("学习行为类型")
    private String behavior;

    @ApiModelProperty("学习行为分值")
    private double behaviorScore;
}