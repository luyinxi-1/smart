package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "ClassChapterMasteryReturnParam", description = "班级章节掌握情况返回参数")
public class ClassChapterMasteryReturnParam {
    @ApiModelProperty("班级ID")
    private Long classId;

    @ApiModelProperty("班级名称")
    private String className;

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("学生总数")
    private Integer totalStudents;

    @ApiModelProperty("参与答题的学生数")
    private Integer participatingStudents;

    @ApiModelProperty("各章节掌握度详情")
    private List<ChapterMasteryDetail> chapterDetails;

    @Data
    @ApiModel("章节掌握度详情")
    public static class ChapterMasteryDetail {
        @ApiModelProperty("章节ID")
        private Long chapterId;

        @ApiModelProperty("章节名称")
        private String chapterName;

        @ApiModelProperty("班级平均掌握度")
        private Double averageMastery;

        @ApiModelProperty("掌握度等级")
        private String masteryLevel;

        @ApiModelProperty("完全掌握学生数")
        private Integer excellentStudents;

        @ApiModelProperty("良好掌握学生数")
        private Integer goodStudents;

        @ApiModelProperty("需要提高学生数")
        private Integer needImprovementStudents;
    }
}
