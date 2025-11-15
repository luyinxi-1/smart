package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel(description = "批量同步确认参数")
public class BatchSyncConfirmationDto {

    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;

    @ApiModelProperty(value = "需要确认同步的教材ID列表", required = true)
    private List<Long> textbookIds;

    @ApiModelProperty(value = "已成功同步的数据主键ID列表", required = true)
    private List<Long> syncedIds;
}