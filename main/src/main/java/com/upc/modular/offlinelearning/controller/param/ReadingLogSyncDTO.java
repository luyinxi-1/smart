package com.upc.modular.offlinelearning.controller.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ReadingLogSyncDTO {

    private String clientUuid;

    private Long studentId;

    private Long textbookId;

    private Long textbookCatalogId;

    private LocalDateTime startTime;

    private Integer durationMinutes;
}
