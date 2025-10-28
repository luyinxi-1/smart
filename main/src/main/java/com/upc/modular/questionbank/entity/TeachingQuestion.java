package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Data
@Accessors(chain = true)
@TableName("teaching_question")
@ApiModel(value = "TeachingQuestion对象", description = "")
public class TeachingQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("题型")
    @TableField("type")
    private Integer type;

    @ApiModelProperty("题目内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("难度等级")
    @TableField("difficulty")
    private Integer difficulty;

    @ApiModelProperty("状态（0禁用，1启用）")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("题目答案")
    @TableField("answer")
    private String answer;

    @ApiModelProperty("创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @ApiModelProperty("所属学科")
    @TableField("subject")
    private String subject;

    @ApiModelProperty("题目分类")
    @TableField("teaching_question_classification_id")
    private String teachingQuestionClassificationId;

    @ApiModelProperty("选择题选项")
    @TableField("choice_question_options")
    private String choiceQuestionOptions;

    @ApiModelProperty("答案解析")
    @TableField("answer_analysis")
    private String answerAnalysis;

    @ApiModelProperty("所属教材id")
    @TableField("teaching_material_id")
    private Long teachingMaterialId;

    @ApiModelProperty("所属章节id")
    @TableField("chapter_id")
    private Long chapterId;

    @ApiModelProperty("创建人名称")
    @TableField(exist = false) // 非数据库字段
    private String creatorName;

    @ApiModelProperty("所属教材名称")
    @TableField(exist = false) // 非数据库字段
    private String teachingMaterialName;

    @ApiModelProperty("所属章节名称")
    @TableField(exist = false) // 非数据库字段
    private String chapterName;

    @ApiModelProperty("是否为当前用户创建")
    @TableField(exist = false) // 非数据库字段
    private Boolean isCreatedByCurrentUser;
}
