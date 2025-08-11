package com.upc.modular.teacher.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(value = "批量修改状态参数")
public class BatchUpdateStatusDto {
    @ApiModelProperty(value = "学生主键ID列表", required = true)
    private List<Long> ids;

    @ApiModelProperty(value = "要更新的账号状态", required = true)
    private Integer accountStatus;
}
