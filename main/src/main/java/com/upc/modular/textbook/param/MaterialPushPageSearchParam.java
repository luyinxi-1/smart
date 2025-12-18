package com.upc.modular.textbook.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "资料推送分页查询参数")
public class MaterialPushPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("教材ID")
    private Long textbookId;
    
    @ApiModelProperty("章节ID")
    private Long textbookCatalogId;
    
    @ApiModelProperty("资料名称")
    private String name;
}