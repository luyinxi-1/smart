package com.upc.modular.materials.entity;

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
 * 应用素材与教学素材关联表
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("application_materials_mapping")
@ApiModel(value = "ApplicationMaterialsMapping对象", description = "应用素材与教学素材关联表")
public class ApplicationMaterialsMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("应用素材ID")
    @TableField("application_material_id")
    private Long applicationMaterialId;

    @ApiModelProperty("教学素材ID")
    @TableField("teaching_material_id")
    private Long teachingMaterialId;

    @ApiModelProperty("排序序号")
    @TableField("sequence")
    private Integer sequence;

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
