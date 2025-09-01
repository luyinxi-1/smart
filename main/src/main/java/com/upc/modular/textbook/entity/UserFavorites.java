package com.upc.modular.textbook.entity;

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
 * @author fwx
 * @since 2025-08-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("user_favorites")
@ApiModel(value = "UserFavorites对象", description = "")
public class UserFavorites implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户id")
    @TableField(value ="user_id")
    private Long userId;

    @ApiModelProperty("书籍id")
    @TableField(value ="textbook_id")
    private Long textbookId;

    @ApiModelProperty("教材分类")
    @TableField(value ="classification")
    private Long classification;

    @ApiModelProperty("书籍名称")
    @TableField(value ="textbook_name")
    private String textbookName;

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
    @TableField(value ="operator_datetime")
    private LocalDateTime operatorDatetime;


}
