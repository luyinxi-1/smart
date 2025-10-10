package com.upc.modular.textbook.entity;

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
 * @since 2025-07-08
 */
@Data
@Accessors(chain = true)
@TableName("textbook_catalog")
@ApiModel(value = "TextbookCatalog对象", description = "")
public class TextbookCatalog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，目录的唯一标识")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教材主表的id")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("目录的名称")
    @TableField("catalog_name")
    private String catalogName;

    @ApiModelProperty("该目录下的正文内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("目录的级别（1 2 3 4四个级别）")
    @TableField("catalog_level")
    private Integer catalogLevel;

    @ApiModelProperty("当前目录父级目录的id")
    @TableField("father_catalog_id")
    private Long fatherCatalogId;

    @ApiModelProperty("目录的排序（位置）")
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty("创建人（创建该目录记录的用户）")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人（最近操作该目录记录的用户）")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @ApiModelProperty("章节uuid")
    @TableField(value = "catalog_uuid")
    private String catalogUuid;

}
