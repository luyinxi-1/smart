package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "TextbookStudentRankParam", description = "教材学生阅读排行榜")
public class TextStudentRankParam {
    @ApiModelProperty("学生姓名")
    private String student_name;

    @ApiModelProperty("阅读时长")
    private Long read_time;

    @ApiModelProperty("阅读排名")
    private Integer rank;

    @ApiModelProperty("用户ID")
    private Long user_id;
}
