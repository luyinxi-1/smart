package com.upc.modular.auth.controller.param.SysDictTypeParam;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class SysDictTypeSearchParam extends PageBaseSearchParam {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("字典主键")
    private Long dictTypeId;

    @ApiModelProperty("字典名称")
    private String dictTypeName;

    @ApiModelProperty("字典唯一编码")
    private String dictTypeCode;

    @ApiModelProperty("状态（0正常 1停用）")
    private String status;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;
}
