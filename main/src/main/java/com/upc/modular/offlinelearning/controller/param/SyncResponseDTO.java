package com.upc.modular.offlinelearning.controller.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SyncResponseDTO {
    private List<SyncResultDTO> results;
}
