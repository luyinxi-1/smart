package com.upc.modular.ai.param;

import com.upc.modular.ai.entity.AiConversationRecords;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AIConRecordsSessionId extends AiConversationRecords {
    @ApiModelProperty("会话id")
    private String sessionId;
}
