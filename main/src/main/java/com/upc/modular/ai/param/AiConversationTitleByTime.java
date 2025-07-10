package com.upc.modular.ai.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
@Data
@Accessors(chain = true)
public class AiConversationTitleByTime {
    // 今天的对话标题
    private List<AiConversationTitle> today;
    // 昨天的对话标题
    private List<AiConversationTitle> yesterday;
    // 7天内（不包含今天和昨天）的对话标题
    private List<AiConversationTitle> last7Days;
    // 30天内（不包含7天内的）的对话标题
    private List<AiConversationTitle> last30Days;
    // 30天以外的对话标题，按月份分组，key 格式为 yyyy.MM
    private List<AiConversationOtherMonth> otherMonths;
}
