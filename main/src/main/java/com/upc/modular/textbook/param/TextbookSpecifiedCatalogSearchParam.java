package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
public class TextbookSpecifiedCatalogSearchParam {

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("目录id")
    private Long catalogId;

}
