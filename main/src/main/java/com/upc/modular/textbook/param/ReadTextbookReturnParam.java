package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.TextbookCatalog;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: xth
 * @Date: 2025/8/29 10:19
 */
@Data
public class ReadTextbookReturnParam {

    @ApiModelProperty("去除html标签")
    private List<TextbookCatalog> textbookCatalogListWithoutHtml;

    @ApiModelProperty("带有html标签")
    private List<TextbookCatalog> textbookCatalogListWithHtml;
}
