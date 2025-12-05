package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentReadingRankParam对象", description = "学生阅读排名参数")
public class StudentReadingRankParam {
    @ApiModelProperty("班级名称")
    private String groupName;
    
    @ApiModelProperty("班级ID")
    private Long groupId;
    
    @ApiModelProperty("学生姓名")
    private String studentName;
    
    @ApiModelProperty("学生ID")
    private Long studentId;
    
    @ApiModelProperty("阅读量（该学生看所有书的总计阅读量）")
    private Long readingCount;
    
    @ApiModelProperty("排名（按阅读量排名）")
    private Long rank;

    @ApiModelProperty("学生行为")
    private String behavior;
}