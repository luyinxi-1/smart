package com.upc.modular.ai.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.ai.entity.AiConversation;
import com.upc.modular.ai.param.AIConRecordsSessionId;
import com.upc.modular.ai.param.AiConversationTitleByTime;
import com.upc.modular.ai.service.IAiConversationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-09
 */
@RestController
@RequestMapping("/ai-conversation")
@Api(tags = "ai对话")
public class AiConversationController {
    @Autowired
    private IAiConversationService aiConversationService;

    @ApiOperation("新增主表记录")
    @PostMapping("/createConversationIfNeeded")
    public R<Long> createConversationIfNeeded(@RequestBody AIConRecordsSessionId param){
        Long result = aiConversationService.createConversationIfNeeded(param);
        return R.ok(result);
    }

    @ApiOperation("删除子表相关记录")
    @PostMapping("/deleteChildTableRecord")
    public R deleteChildTableRecord(@RequestBody AiConversation param){
        Boolean result = aiConversationService.deleteChildTableRecord(param);
        return R.ok(result);
    }

    @ApiOperation("修改对话标题")
    @PostMapping("updateMainTableTitle")
    public R<Integer> updateMainTableTitle(@RequestBody AiConversation param){
        Integer result = aiConversationService.updateMainTableTitle(param);
        return R.ok(result);
    }

    @ApiOperation("查询用户各个时间段对话标题")
    @GetMapping("/selectConversionTitleByTime")
    public R<AiConversationTitleByTime> selectConversionTitleByTime(){
        AiConversationTitleByTime result = aiConversationService.selectConversionTitleByTime();
        return R.ok(result);
    }
}
