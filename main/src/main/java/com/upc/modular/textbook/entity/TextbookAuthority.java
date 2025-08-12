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
 * @author byh
 * @since 2025-07-08
 */
@Data
@Accessors(chain = true)
@TableName("textbook_authority")
@ApiModel(value = "TextbookAuthority对象", description = "")
public class TextbookAuthority implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，权限记录的唯一标识")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教材主表的id，关联教材表")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("权限类型，1表示协作者，2表示可见机构")
    @TableField("authority_type")
    private Integer authorityType;

    @ApiModelProperty("共同协作者（教师id），仅在权限类型为协作者时有效")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("可见机构id，仅在权限类型为可见机构时有效")
    @TableField("visible_institute_id")
    private Long visibleInstituteId;

    @ApiModelProperty("创建人（创建该权限记录的用户）")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人（最近操作该权限记录的用户）")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
