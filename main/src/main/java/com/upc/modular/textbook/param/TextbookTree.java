package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TextbookTree {
    @ApiModelProperty("教材主表的id")
    private Long textbookId;

    @ApiModelProperty("目录的名称")
    private String catalogName;

    @ApiModelProperty("目录的级别（1 2 3 4四个级别）")
    private Integer catalogLevel;

    @ApiModelProperty("当前目录父级目录的id")
    private Long fatherCatalogId;

    @ApiModelProperty("目录的排序（位置）")
    private Integer sort;

    @ApiModelProperty("子目录")
    private List<TextbookTree> children;
}
