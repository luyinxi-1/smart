package com.upc.modular.questionbank.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 题库素材关联表
 * </p>
 *
 * @author cyy
 * @since 2025-10-27
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("question_bank_materials_mapping")
@ApiModel(value = "QuestionBankMaterialsMapping对象", description = "题库素材关联表")
public class QuestionBankMaterialsMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("题库id")
    @TableField("question_bank_id")
    private Long questionBankId;

    @ApiModelProperty("教学素材id")
    @TableField("material_id")
    private Long materialId;

    @ApiModelProperty("排序序号")
    @TableField("sequence")
    private Integer sequence;

    @ApiModelProperty("创建者")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作者")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;
}

