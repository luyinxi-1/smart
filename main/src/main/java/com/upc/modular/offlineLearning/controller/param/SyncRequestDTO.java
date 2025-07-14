package com.upc.modular.offlineLearning.controller.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SyncRequestDTO {
    private List<ReadingLogSyncDTO> readingLogsToSync;
}
