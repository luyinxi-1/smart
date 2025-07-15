package com.upc.modular.offlinelearning.controller.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SyncRequestDTO {
    private List<ReadingLogSyncDTO> readingLogsToSync;
}
