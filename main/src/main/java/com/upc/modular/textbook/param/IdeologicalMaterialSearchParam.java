package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/8 20:34
 */
@Data
public class IdeologicalMaterialSearchParam {

    @ApiModelProperty("关联教材id，对应教材主表")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("关联教材目录id，对应教材目录表")
    @TableField("textbook_catalog_id")
    private Long textbookCatalogId;

    @ApiModelProperty("思政资料的名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("思政资料的类型（如思想政治理论文章、时事政治解读等）")
    @TableField("type")
    private String type;
}
