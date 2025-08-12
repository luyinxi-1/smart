package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author la
 * @since 2025-08-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("teaching_question_classification")
@ApiModel(value = "TeachingQuestionClassification对象", description = "")
public class TeachingQuestionClassification implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("题目分类表")
    @TableId("id")
    private Long id;

    @ApiModelProperty("题目分类名称")
    @TableField("teaching_question_classification_name")
    private String teachingQuestionClassificationName;

    @ApiModelProperty("题目排序序号")
    @TableField("sort_number")
    private Integer sortNumber;

    @ApiModelProperty("父id（上一级的题目分类id）")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty("产品分类等级(1表示一级分类，2表示二级分类，3表示三级分类)")
    @TableField("classification_grade")
    private Integer classificationGrade;

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

    @TableField(exist = false)
    private List<TeachingQuestionClassification> children;
}
