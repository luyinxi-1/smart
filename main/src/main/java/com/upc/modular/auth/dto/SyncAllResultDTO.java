package com.upc.modular.auth.dto;

import lombok.Data;

@Data
public class SyncAllResultDTO {
    private SyncResultDTO studentResult;
    private SyncResultDTO teacherResult;

    // 可选：汇总字段（想要就留，不想要就删）
    private Integer totalInsertCount;
    private Integer totalUpdateCount;
}

