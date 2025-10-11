package com.upc.modular.textbook.entity;

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
 * @author la
 * @since 2025-07-15
 */
@Data
@Accessors(chain = true)
@TableName("learning_annotations_and_labels")
@ApiModel(value = "LearningAnnotationsAndLabels对象", description = "")
public class LearningAnnotationsAndLabels implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("批注和标注内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("关联的教材ID")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("关联的教材目录正文ID")
    @TableField("catalog_id")
    private Long catalogId;

    @ApiModelProperty("创建人ID")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人ID")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @ApiModelProperty("批注和标注在文章中的位置信息")
    @TableField("position_info")
    private String positionInfo;
}
