package com.upc.modular.textbook.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
 * @since 2025-08-12
 */
@Data
@Accessors(chain = true)
@TableName("textbook_classification")
@ApiModel(value = "TextbookClassification对象", description = "")
public class TextbookClassification implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("分类名称")
    @TableField("classification_name")
    private String classificationName;

    @ApiModelProperty("排序")
    @TableField("sort_number")
    private Integer sortNumber;

    @ApiModelProperty("父id（上一级的分类id）")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty("产品分类等级(1表示一级分类，2表示二级分类，3表示三级分类)")
    @TableField("classification_grade")
    private Integer classificationGrade;

    @ApiModelProperty("创建人（创建该记录的用户）")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人（最近操作该记录的用户）")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @TableField(exist = false)
    private List<TextbookClassification> children;


}
