package com.upc.modular.ai.param;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AiConversationTitle {
    // 主表 id
    private Long id;
    // 对话标题
    private String title;
    //会话标题
    private String conversationId;
}
