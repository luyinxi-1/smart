package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class OfflineReadingLogParam {
    @ApiModelProperty("学生id")
    private Long studentId;

    @ApiModelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("章节id")
    private Long textbookCatalogId;

    @ApiModelProperty("阅读开始时间(ISO)")
    private LocalDateTime startTime;

    @ApiModelProperty("阅读时长(分钟)")
    private Integer durationMinutes;

    @ApiModelProperty("客户端生成的唯一UUID")
    private String clientUuid;

    @ApiModelProperty("客户端写入的本地创建时间")
    private LocalDateTime addDatetime;
}
