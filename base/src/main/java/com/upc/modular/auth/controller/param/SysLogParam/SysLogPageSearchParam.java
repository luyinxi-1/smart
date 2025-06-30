package com.upc.modular.auth.controller.param.SysLogParam;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SysLogPageSearchParam extends PageBaseSearchParam {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户名称")
    private String name;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;
}
