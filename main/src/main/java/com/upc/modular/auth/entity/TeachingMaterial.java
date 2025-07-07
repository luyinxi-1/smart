package com.upc.modular.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@TableName("teaching_material")
@ApiModel(value = "TeachingMaterial对象", description = "")
public class TeachingMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("绑定的实体id")
    @TableField("object_id")
    private Long objectId;

    @ApiModelProperty("绑定的实体类型")
    @TableField("object_type")
    private String objectType;

    @ApiModelProperty("教学资料名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("教学资料类型")
    @TableField("type")
    private String type;

    @ApiModelProperty("文件属性")
    @TableField("file_attribute")
    private String fileAttribute;

    @ApiModelProperty("文件路径")
    @TableField("file_path")
    private String filePath;

    @ApiModelProperty("介绍信息")
    @TableField("introduction")
    private String introduction;

    @ApiModelProperty("作者id")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("文件大小")
    @TableField("file_size")
    private Integer fileSize;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
