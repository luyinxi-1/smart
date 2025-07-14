package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.TextbookCatalog;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/12 10:57
 */
@Data
public class TextbookCatalogDto extends TextbookCatalog {

    private TextbookCatalog parent;
}
