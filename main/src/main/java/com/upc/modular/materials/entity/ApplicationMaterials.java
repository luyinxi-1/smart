package com.upc.modular.materials.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 应用素材实体类
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("application_materials")
@ApiModel(value = "ApplicationMaterials对象", description = "应用素材")
public class ApplicationMaterials implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("应用素材名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("应用素材描述")
    @TableField("description")
    private String description;

    @ApiModelProperty("题库ID")
    @TableField("question_bank_id")
    private Long questionBankId;

    @ApiModelProperty("发布状态（0:未发布，1:已发布）")
    @TableField("status")
    private Integer status;

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
    
    // 非数据库字段，用于前端传递和接收关联的教学素材ID
    @TableField(exist = false)
    private List<Long> teachingMaterialIds;
}
