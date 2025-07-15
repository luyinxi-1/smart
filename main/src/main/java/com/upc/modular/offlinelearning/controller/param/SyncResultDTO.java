package com.upc.modular.offlinelearning.controller.param;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SyncResultDTO {
    private String clientUuid;

    private boolean success;

    /**
     * 如果同步失败，这里可以包含失败的原因。
     * 例如："Conflict: record has been modified by another device."
     * 如果成功，但有特殊信息，也可以放在这里，例如："Record already exists, skipped."
     */
    private String message;
}
