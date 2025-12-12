package com.upc.modular.auth.dto;
// com.upc.modular.user.dto.SyncResultDTO
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("批量同步结果")
public class SyncResultDTO {

    @ApiModelProperty("新增数量")
    private int insertCount;

    @ApiModelProperty("更新数量")
    private int updateCount;

    @ApiModelProperty("新增列表")
    private List<SyncItemDTO> insertedList;

    @ApiModelProperty("更新列表")
    private List<SyncItemDTO> updatedList;
}
