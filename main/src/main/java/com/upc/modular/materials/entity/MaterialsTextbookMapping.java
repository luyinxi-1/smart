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
 * @since 2025-08-23
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("materials_textbook_mapping")
@ApiModel(value = "MaterialsTextbookMapping对象", description = "")
public class MaterialsTextbookMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教学素材id")
    @TableField("material_id")
    private Long materialId;

    @ApiModelProperty("教材id")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("素材所在章名")
    @TableField("chapter_name")
    private String chapterName;

    @ApiModelProperty("素材所在章id")
    @TableField("chapter_id")
    private Long chapterId;

    @ApiModelProperty("教材中素材在线浏览次数")
    @TableField("view_count")
    private Long viewCount;

    @ApiModelProperty("教材中素材下载次数")
    @TableField("download_count")
    private Long downloadCount;

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
