package com.upc.modular.textbook.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.entity.LearningNotes;
import com.upc.modular.textbook.param.*;
import com.upc.modular.textbook.service.ILearningLogService;
import io.lettuce.core.output.BooleanListOutput;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
@RestController
@RequestMapping("/learning-log")
@Api(tags = "学习日志")
public class LearningLogController {

    @Autowired
    private ILearningLogService learningLogService;

    @ApiOperation(value = "新增学习日志")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody LearningLog learningLog) {
       return R.ok(learningLogService.insert(learningLog));
    }

    @ApiOperation(value = "最近学习")
    @PostMapping("/recentStudy")
    public R<List<RecentStudyReturnParam>> recentStudy(@RequestParam("limit") Integer limit) {
        return R.ok(learningLogService.recentStudy(limit));
    }
    
    @ApiOperation(value = "根据UUID批量删除学习日志（客户端删除使用)")
    @PostMapping("batchDeleteByUuid")
    public R<Boolean> batchDeleteByUuid(@RequestBody UuidParam uuidParam) {
        return R.ok(learningLogService.batchDeleteByUuid(uuidParam));
    }
    @ApiOperation(value = "检查服务端是否存在具有指定 clientUuid 的学习批注和标注数据(客户端用)")
    @PostMapping("/checkExistByUuid")
    public R<Boolean> checkExistByUuid(@RequestBody String clientUuid) {
        if (StringUtils.isEmpty(clientUuid)) {
            return R.ok(false);
        }
        // 查询数据库中是否存在该clientUuid的学习日志
        LambdaQueryWrapper<LearningLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningLog::getClientUuid, clientUuid);

        boolean exists = learningLogService.count(queryWrapper) > 0;
        return R.ok(exists);
    }
    @ApiOperation(value = "获取指定用户在多本书籍下需要同步的笔记(客户端用)")
    @PostMapping("/getNewLogsBatch")
    public R<List<LearningLog>> getNewLogsBatch(@RequestBody BatchSyncRequestDto requestDto) {
        return R.ok(learningLogService.getNewLogsBatch(requestDto.getUserId(), requestDto.getTextbookIds()));
    }

    @ApiOperation(value = "确认指定用户在多本书籍下的笔记已同步(客户端用)")
    @PostMapping("/confirmLogsSyncBatch")
    public R<Void> confirmLogsSyncBatch(@RequestBody BatchSyncConfirmationDto confirmationDto) {
        boolean success = learningLogService.confirmLogsSyncBatch(
                confirmationDto.getUserId(),
                confirmationDto.getTextbookIds(),
                confirmationDto.getSyncedIds()
        );
        return success ? R.ok() : R.fail("批量确认同步失败");
    }
}