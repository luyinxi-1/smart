package com.upc.modular.datastatistics.controller.param;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
/**
 * 系统核心数据统计DTO
 */
@Data
@ApiModel(description = "系统所有核心数据统计响应模型")
public class ExportSystemStatisticsParm {

    @ExcelProperty("教师数量")
    @ApiModelProperty(value = "教师数量")
    private Long teacherCount;
    @ExcelProperty("学生数量")
    @ApiModelProperty(value = "学生数量")
    private Long studentCount;
    @ExcelProperty("班级数量")
    @ApiModelProperty(value = "班级数量")
    private Long groupCount;
    @ExcelProperty("教学思政数量")
    @ApiModelProperty(value = "教学思政数量")
    private Long teachingideologicalMaterialCount;
    @ExcelProperty("教学活动数量")
    @ApiModelProperty(value = "教学活动数量")
    private Long discussionTopicCount;
    @ExcelProperty("交流反馈数量")
    @ApiModelProperty(value = "交流反馈数量")
    private Long discussionTopicReplyCount;
    @ExcelProperty("题库数量")
    @ApiModelProperty(value = "题库数量")
    private Long teachingQuestionBankCount;
    @ExcelProperty("在售课程数量")
    @ApiModelProperty(value = "在授课程数量")
    private Long courseCount;
    @ExcelProperty("教学素材数量")
    @ApiModelProperty(value = "教学素材数量")
    private Long teachingMaterialsCount;
    @ExcelProperty("智慧教材数量(已发布)")
    @ApiModelProperty(value = "智慧教材数量 (已发布)")
    private Long textbookCount;
   /* @ExcelProperty("某日总学习时长(分钟)")
    @ApiModelProperty(value = "某日总学习时长(分钟)")
    private Long todayStudyTime;
    @ExcelProperty("某日访问人数")
    @ApiModelProperty(value = "某日访问人数")
    private Long todayVisitorCount;*/
}
