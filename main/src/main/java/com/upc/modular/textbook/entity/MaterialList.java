package com.upc.modular.textbook.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author JM
 * @since 2025-10-29
 */
@Data
@Accessors(chain = true)
@TableName("material_list")
@ApiModel(value = "MaterialList对象", description = "")
public class MaterialList {
    @ApiModelProperty("主键，资料唯一标识")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("列表资料的名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("列表资料的类型")
    @TableField("type")
    private String type;

    @ApiModelProperty("列表资料的地址")
    @TableField("address")
    private String address;

    @ApiModelProperty("创建人（列表资料创建者）")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人（最近操作该资料的用户）")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @ApiModelProperty("推送资料表id")
    @TableField(value = "material_push_id")
    private Long materialPushId;
}
