package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "ClassAnalysisReturnParam", description = "班级分析报告返回参数")
public class ClassAnalysisReturnParam {
    @ApiModelProperty("班级ID")
    private Long classId;

    @ApiModelProperty("班级名称")
    private String className;

    @ApiModelProperty("学生总数")
    private Integer totalStudents;

    @ApiModelProperty("班级总阅读时长")
    private Long totalReadingTime;

    @ApiModelProperty("班级平均阅读时长")
    private Long averageReadingTime;

    @ApiModelProperty("班级总阅读数量")
    private Long totalReadingNum;

    @ApiModelProperty("班级平均阅读数量")
    private Long averageReadingNum;

    @ApiModelProperty("班级完成教材阅读总数")
    private Long totalCompletionReadingNum;

    @ApiModelProperty("班级平均完成教材阅读数量")
    private Long averageCompletionReadingNum;

    @ApiModelProperty("班级笔记总数")
    private Long totalNotesNum;

    @ApiModelProperty("班级平均笔记数量")
    private Long averageNotesNum;

    @ApiModelProperty("班级答题题库总数")
    private Long totalQuestionBankNum;

    @ApiModelProperty("班级平均答题题库数量")
    private Long averageQuestionBankNum;

    @ApiModelProperty("班级交流反馈总数")
    private Long totalCommunicationFeedbackNum;

    @ApiModelProperty("班级平均交流反馈数量")
    private Long averageCommunicationFeedbackNum;

    @ApiModelProperty("统计时间范围")
    private String timeRange;
}