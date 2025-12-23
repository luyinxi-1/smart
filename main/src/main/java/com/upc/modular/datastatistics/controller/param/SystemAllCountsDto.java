package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统核心数据统计DTO
 */
@Data
@ApiModel(description = "系统所有核心数据统计响应模型")
public class SystemAllCountsDto {

    @ApiModelProperty(value = "教师数量", example = "150")
    private Long teacherCount;

    @ApiModelProperty(value = "学生数量", example = "2000")
    private Long studentCount;

    @ApiModelProperty(value = "班级数量", example = "50")
    private Long groupCount;

    @ApiModelProperty(value = "教学思政数量", example = "100")
    private Long teachingideologicalMaterialCount;

    @ApiModelProperty(value = "教学活动数量", example = "80")
    private Long discussionTopicCount;

    @ApiModelProperty(value = "交流反馈数量", example = "500")
    private Long discussionTopicReplyCount;

    @ApiModelProperty(value = "题库数量", example = "30")
    private Long teachingQuestionBankCount;

    @ApiModelProperty(value = "在授课程数量", example = "45")
    private Long courseCount;

    @ApiModelProperty(value = "教学素材数量", example = "1200")
    private Long teachingMaterialsCount;

    @ApiModelProperty(value = "智慧教材数量 (已发布)", example = "25")
    private Long textbookCount;

    @ApiModelProperty(value = "某日总学习时长(小时)", example = "30")
    private double todayStudyTime;

    @ApiModelProperty(value = "某日访问人数", example = "350")
    private Long todayVisitorCount;

    @ApiModelProperty("教材收藏数量（已发布教材被收藏次数）")
    private Long textbookFavoriteCount;

}