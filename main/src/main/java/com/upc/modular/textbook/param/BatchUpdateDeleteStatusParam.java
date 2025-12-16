package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("批量更新教材删除状态参数")
public class BatchUpdateDeleteStatusParam {
    
    @ApiModelProperty(value = "教材ID列表", required = true)
    private List<Long> ids;
    
    @ApiModelProperty(value = "删除状态 1:已删除 0:未删除", required = true)
    private Integer isDelete;
}