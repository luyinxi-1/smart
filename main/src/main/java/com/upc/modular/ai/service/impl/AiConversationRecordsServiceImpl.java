package com.upc.modular.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.modular.ai.entity.AiConversation;
import com.upc.modular.ai.entity.AiConversationRecords;
import com.upc.modular.ai.mapper.AiConversationRecordsMapper;
import com.upc.modular.ai.param.AIConRecordsSessionId;
import com.upc.modular.ai.service.IAiConversationRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.ai.service.IAiConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-09
 */
@Service
public class AiConversationRecordsServiceImpl extends ServiceImpl<AiConversationRecordsMapper, AiConversationRecords> implements IAiConversationRecordsService {
    @Autowired
    private IAiConversationService aiConversationService;
    @Override
    public List<AiConversationRecords> selectDeepseekConRecords(Long dpConId) {
        QueryWrapper<AiConversationRecords> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ai_conversation_id", dpConId)
                .orderByAsc("add_datetime"); // 按时间从远到近排序

        return this.list(queryWrapper); // 返回列表
    }

    @Override
    public Long insertDeepseekConRecords(AIConRecordsSessionId deepseekConRecordsSessionId) {
        Long dpConId = deepseekConRecordsSessionId.getAiConversationId();
        System.out.println(deepseekConRecordsSessionId);
        if (dpConId == null || aiConversationService.getById(dpConId) == null) {
            Long newMainId = aiConversationService.createConversationIfNeeded(deepseekConRecordsSessionId);
            dpConId = newMainId; // 赋值新的主表 ID
        }

        // 插入子表记录
        AiConversationRecords newdeepseekConRecords = new AiConversationRecords()
                .setAiConversationId(dpConId)
                .setContent(deepseekConRecordsSessionId.getContent())
                .setRole(deepseekConRecordsSessionId.getRole())
                .setContentThink(deepseekConRecordsSessionId.getContentThink())
                .setSearch(deepseekConRecordsSessionId.getSearch())
                .setCreator(deepseekConRecordsSessionId.getCreator())
                .setAddDatetime(deepseekConRecordsSessionId.getAddDatetime())
                .setOperator(deepseekConRecordsSessionId.getOperator())
                .setOperationDatetime(deepseekConRecordsSessionId.getOperationDatetime());

        this.save(newdeepseekConRecords);
        if (StringUtils.isNotBlank(deepseekConRecordsSessionId.getSessionId())) {
            AiConversation update = new AiConversation();
            update.setId(dpConId);
            update.setConversationId(deepseekConRecordsSessionId.getSessionId());
            aiConversationService.updateById(update);
        }
        return dpConId;
    }

}
