package com.upc.modular.homepage.entity;

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
 * @since 2025-07-15
 */
@Data
@Accessors(chain = true)
@TableName("home_page_notice")
@ApiModel(value = "HomePageNotice对象", description = "")
public class HomePageNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("通知公告的标题")
    @TableField("title")
    private String title;

    @ApiModelProperty("通知公告的封面图片")
    @TableField("picture")
    private String picture;

    @ApiModelProperty("通知公告类型")
    @TableField("type")
    private Integer type;

    @ApiModelProperty("通知公告的内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("是否置顶（0 否，1 是）")
    @TableField("is_top")
    private Integer isTop;

    @ApiModelProperty("是否发布(0是未发布 1是发布)")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("公示截止日期")
    @TableField("deadline")
    private LocalDateTime deadline;

    @ApiModelProperty("副标题")
    @TableField("subtitle")
    private String subtitle;

    @ApiModelProperty("通知公告创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("通知公告创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("通知公告操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("通知公告操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
