package com.upc.modular.client.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SyncInfoReturnParam {

    @ApiModelProperty("同步信息")
    private String syncInfo;

    @ApiModelProperty("同步时间")
    private LocalDateTime syncTime;

    @ApiModelProperty("同步用户id")
    private Long syncUserId;

    @ApiModelProperty("失败列表Map{id, 失败原因}")
    private Map<Long, String> failMap;
}
