package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "ClassBehaviorAnalysisReturnParam", description = "班级学习行为分析返回参数")
public class ClassBehaviorAnalysisReturnParam {
    @ApiModelProperty("班级ID")
    private Long classId;

    @ApiModelProperty("班级名称")
    private String className;

    @ApiModelProperty("学生总数")
    private Integer totalStudents;

    @ApiModelProperty("班级整体规律性分数")
    private Double classRegularityScore;

    @ApiModelProperty("班级整体学习习惯类型")
    private String classHabitType;

    @ApiModelProperty("优秀学习习惯学生数")
    private Integer excellentHabitStudents;

    @ApiModelProperty("良好学习习惯学生数")
    private Integer goodHabitStudents;

    @ApiModelProperty("需要改善学习习惯学生数")
    private Integer needImprovementStudents;

    @ApiModelProperty("学生行为分析详情")
    private List<StudentBehaviorDetail> studentDetails;

    @ApiModelProperty("分析建议")
    private String recommendation;

    @Data
    @ApiModel("学生行为分析详情")
    public static class StudentBehaviorDetail {
        @ApiModelProperty("学生ID")
        private Long studentId;

        @ApiModelProperty("学生姓名")
        private String studentName;

        @ApiModelProperty("学生规律性分数")
        private Double regularityScore;

        @ApiModelProperty("学生学习习惯类型")
        private String habitType;
    }
}