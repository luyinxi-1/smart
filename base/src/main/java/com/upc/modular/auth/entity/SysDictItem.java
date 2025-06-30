package com.upc.modular.auth.entity;

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
 * @since 2025-06-26
 */
@Data
@Accessors(chain = true)
@TableName("sys_dict_item")
@ApiModel(value = "SysDictItem对象", description = "")
public class SysDictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("字典类型编码")
    @TableField("dict_type_code")
    private String dictTypeCode;

    @ApiModelProperty("字典项值")
    @TableField("dict_item_value")
    private String dictItemValue;

    @ApiModelProperty("字典项名称")
    @TableField("dict_item_name")
    private String dictItemName;

    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("顺序")
    @TableField("item_order")
    private Integer itemOrder;

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
