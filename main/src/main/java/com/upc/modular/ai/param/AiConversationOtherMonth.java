package com.upc.modular.ai.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
@Data
@Accessors(chain = true)
public class AiConversationOtherMonth {
    // 日期格式： yyyy-MM
    private String date;
    // 对应月份的对话标题列表
    private List<AiConversationTitle> list;
}
