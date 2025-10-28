package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.param.RecentStudyReturnParam;
import com.upc.modular.textbook.param.UuidParam;
import com.upc.modular.textbook.service.ILearningLogService;
import io.lettuce.core.output.BooleanListOutput;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
}