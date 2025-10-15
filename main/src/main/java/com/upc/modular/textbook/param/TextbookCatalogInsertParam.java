package com.upc.modular.textbook.param;

import com.upc.modular.textbook.entity.TextbookCatalog;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class TextbookCatalogInsertParam extends TextbookCatalog {

    @ApiModelProperty("临时父Id")
    private String temporaryParentId;

    @ApiModelProperty("同级目录的id")
    private Long sameCatalogLevelId;

    @ApiModelProperty("同级目录的uuid")
    private String sameCatalogLevelUuid;

    @ApiModelProperty("是否是数据库为空时新增，0是，1不是")
    private Integer firstAdd;
}
