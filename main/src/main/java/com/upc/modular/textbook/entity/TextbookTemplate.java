package com.upc.modular.textbook.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("textbook_template")
public class TextbookTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("教材ID")
    @TableField(value ="textbook_id")
    private Long textbookId;

    @ApiModelProperty("模板名称")
    @TableField(value ="template_name")
    private String templateName;

    @ApiModelProperty("一级标题样式")
    @TableField(value ="level1_style")
    private String level1Style;

    @ApiModelProperty("二级标题样式")
    @TableField(value ="level2_style")
    private String level2Style;

    @ApiModelProperty("三级标题样式")
    @TableField(value ="level3_style")
    private String level3Style;

    @ApiModelProperty("四级标题样式")
    @TableField(value ="level4_style")
    private String level4Style;

    @ApiModelProperty("主题颜色")
    @TableField(value ="theme_color")
    private String themeColor;

    @ApiModelProperty("状态 0禁用 1启用")
    @TableField(value ="status")
    private Long status;

    @ApiModelProperty("新增时间")
    @TableField("add_datetime")
    private LocalDateTime addDatetime;
}