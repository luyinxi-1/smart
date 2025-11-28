package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
@TableName("teaching_question_bank")
@ApiModel(value = "TeachingQuestionBank对象", description = "")
public class TeachingQuestionBank implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教学题库名称或标题")
    @TableField("name")
    private String name;

    @ApiModelProperty("教学题库说明")
    @TableField("description")
    private String description;

    @ApiModelProperty("教学题库状态（0:表示已关闭，1表示已启用）")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("关联教材ID")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("关联的教材目录Id")
    @TableField("textbook_catalog_id")
    private Long textbookCatalogId;

/*
    @ApiModelProperty("备用教材目录Id")
    @TableField("textbook_catalog_id2")
    private Long textbookCatalogId2;
*/

    @ApiModelProperty("关联的教材目录UUid")
    @TableField(exist = false)
    private String textbookCatalogUuId;

    @ApiModelProperty("是否限制答题次数（0:不限制，1:限制）")
    @TableField("is_limit_attempts")
    private Integer isLimitAttempts;

    @ApiModelProperty("学生可作答的最大次数")
    @TableField("max_attempts")
    private Integer maxAttempts;

    @ApiModelProperty("成绩取法（如0：最高分、1：平均分、2：最后一次）")
    @TableField("score_policy")
    private Integer scorePolicy;

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





}
