package com.upc.modular.ai.service;

import com.upc.modular.ai.entity.AiConversation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.ai.param.AIConRecordsSessionId;
import com.upc.modular.ai.param.AiConversationTitleByTime;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-09
 */
public interface IAiConversationService extends IService<AiConversation> {

    Long createConversationIfNeeded(AIConRecordsSessionId param);

    Boolean deleteChildTableRecord(AiConversation param);

    Integer updateMainTableTitle(AiConversation param);

    AiConversationTitleByTime selectConversionTitleByTime();
}
