package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentAnalysisReturnParam", description = "学生学习路径返回参数")
public class StudentAnalysisReturnParam {
    @ApiModelProperty("学生阅读时长")
    private Long readingTime;
    @ApiModelProperty("学生阅读数量")
    private Long readingNum;
    @ApiModelProperty("学生书架教材数量")
    private Long shelfBookNum;
    @ApiModelProperty("我的课程包含教材数量")
    private Long myCourseBookNum;
    @ApiModelProperty("我的课程教材阅读完成数量")
    private Long myCourseBookCompletionNum;
    @ApiModelProperty("学生已完成教材阅读数量")
    private Long completionReadingNum;
    @ApiModelProperty("学生笔记数量")
    private Long notesNum;
    @ApiModelProperty("学生答题题库数量")
    private Long questionBankNum;
    @ApiModelProperty("学生交流反馈数量")
    private Long communicationFeedbackNum;
}
