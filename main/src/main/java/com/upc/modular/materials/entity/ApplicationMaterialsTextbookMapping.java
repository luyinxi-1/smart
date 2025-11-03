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
 * 应用素材与教材关联表
 * </p>
 *
 * @author system
 * @since 2025-10-31
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("application_materials_textbook_mapping")
@ApiModel(value = "ApplicationMaterialsTextbookMapping对象", description = "应用素材与教材关联表")
public class ApplicationMaterialsTextbookMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("应用素材id")
    @TableField("application_material_id")
    private Long applicationMaterialId;

    @ApiModelProperty("教材id")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("素材所在章名")
    @TableField("chapter_name")
    private String chapterName;

    @ApiModelProperty("素材所在章id")
    @TableField("chapter_id")
    private Long chapterId;

    @ApiModelProperty("教材中应用素材在线浏览次数")
    @TableField("view_count")
    private Long viewCount;

    @ApiModelProperty("教材中应用素材下载次数")
    @TableField("download_count")
    private Long downloadCount;

    @ApiModelProperty("创建者")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作者")
    @TableField(value = "operator", fill = FieldFill.INSERT_UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime operationDatetime;
}

