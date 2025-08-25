package com.upc.modular.homepage.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.upc.config.time.MultiFormatLocalDateTimeDeserializer;
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
 * @since 2025-07-15
 */
@Data
@Accessors(chain = true)
@TableName("home_page_promotion")
@ApiModel(value = "HomePagePromotion对象", description = "")
public class HomePagePromotion implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("宣传内容的标题")
    @TableField("title")
    private String title;

    @ApiModelProperty("宣传封面图的URL路径")
    @TableField("picture")
    private String picture;

    @ApiModelProperty("图文混排的宣传内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("是否置顶（0 否，1 是）")
    @TableField("is_top")
    private Integer isTop = 0;

    @ApiModelProperty("是否发布(0是未发布 1是发布)")
    @TableField("status")
    private Integer status = 0;

    @ApiModelProperty("公示截止日期")
    @TableField("deadline")
    @JsonDeserialize(using = MultiFormatLocalDateTimeDeserializer.class)
    private LocalDateTime deadline;

    @ApiModelProperty("副标题")
    @TableField("subtitle")
    private String subtitle;

    @ApiModelProperty("创建人用户ID")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("宣传内容创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("最后一次操作人用户ID")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("最后修改时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
