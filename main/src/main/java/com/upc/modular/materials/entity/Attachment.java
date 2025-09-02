package com.upc.modular.materials.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
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
 * @author mjh
 * @since 2025-09-02
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("attachment")
@ApiModel(value = "Attachment对象", description = "")
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("文件名")
    @TableField("file_name")
    private String fileName;

    @ApiModelProperty("文件类型")
    @TableField("file_type")
    private String fileType;

    @ApiModelProperty("文件大小（字节数）")
    @TableField("file_size")
    private Double fileSize;

    @ApiModelProperty("文件路径")
    @TableField("file_path")
    private String filePath;

    @ApiModelProperty("绑定的实体类型")
    @TableField("object_type")
    private String objectType;

    @ApiModelProperty("绑定的实体ID")
    @TableField("object_id")
    private Long objectId;

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

    @ApiModelProperty("状态（0正常，1删除/失效）")
    @TableField("status")
    private Integer status;


}
