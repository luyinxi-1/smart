package com.upc.modular.ai.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.ai.entity.AiConversationRecords;
import com.upc.modular.ai.param.AIConRecordsSessionId;
import com.upc.modular.ai.service.IAiConversationRecordsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-09
 */
@RestController
@RequestMapping("/ai-conversation-records")
@Api(tags = "ai对话记录")
public class AiConversationRecordsController {
    @Autowired
    private IAiConversationRecordsService aiConversationRecordsService;
    @PostMapping("/selectDeepseekConRecords")
    @ApiOperation("查询DeepseekConRecords")
    public R<List<AiConversationRecords>> selectDeepseekConRecords(@RequestParam Long dpConId) {
        List<AiConversationRecords> returnList = aiConversationRecordsService.selectDeepseekConRecords(dpConId);

        if (returnList == null || returnList.isEmpty()) {
            return R.fail("未找到相关记录");
        }

        return R.ok(returnList);
    }

    @PostMapping("/insertDeepseekConRecords")
    @ApiOperation("新增DeepseekConRecords")
    @Transactional
    public R<Long> insertDeepseekConRecords(@RequestBody AIConRecordsSessionId DeepseekConRecordsSessionId) {
        Long dpConId = aiConversationRecordsService.insertDeepseekConRecords(DeepseekConRecordsSessionId);
        return R.ok(dpConId);
    }
}
