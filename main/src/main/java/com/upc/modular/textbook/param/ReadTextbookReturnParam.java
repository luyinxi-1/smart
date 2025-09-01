package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.modular.textbook.entity.TextbookCatalog;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: xth
 * @Date: 2025/8/29 10:19
 */
@Data
public class ReadTextbookReturnParam extends TextbookCatalog {

    @ApiModelProperty("目录的名称(纯文字，不带html样式)")
    private String catalogNameWithoutHtml;
}
