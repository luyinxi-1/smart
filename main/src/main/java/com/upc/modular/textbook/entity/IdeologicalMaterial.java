package com.upc.modular.textbook.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @author byh
 * @since 2025-07-08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("ideological_material")
@ApiModel(value = "IdeologicalMaterial对象", description = "")
public class IdeologicalMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，资料唯一标识")
    @TableId("id")
    private Long id;

    @ApiModelProperty("思政资料的名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("思政资料的类型（如思想政治理论文章、时事政治解读等）")
    @TableField("type")
    private String type;

    @ApiModelProperty("思政资料的简介")
    @TableField("introduction")
    private String introduction;

    @ApiModelProperty("思政资料的详细内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("关联教材id，对应教材主表")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("关联教材目录id，对应教材目录表")
    @TableField("textbook_catalog_id")
    private Long textbookCatalogId;

    @ApiModelProperty("创建人（思政资料创建者）")
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


}
