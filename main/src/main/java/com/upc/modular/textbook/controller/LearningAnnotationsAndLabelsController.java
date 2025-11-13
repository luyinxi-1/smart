package com.upc.modular.textbook.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.upc.modular.textbook.param.UuidParam;
import com.upc.modular.textbook.service.ILearningAnnotationsAndLabelsService;
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
 * @author la
 * @since 2025-07-15
 */
@RestController
@RequestMapping("/learning-annotations-and-labels")
@Api(tags = "学习批注和标注")
public class LearningAnnotationsAndLabelsController {

    @Autowired
    private ILearningAnnotationsAndLabelsService learningAnnotationsAndLabelsService;

    @ApiOperation(value = "删除学习标注和批注")
    @PostMapping("/batchDetele")
    public R<Boolean> batchDetele(@RequestBody IdParam idParam) {
        return R.ok(learningAnnotationsAndLabelsService.batchDetele(idParam));
    }
    @ApiOperation(value = "根据UUID批量删除学习标注和批注(客户端用)")
    @PostMapping("/batchDeleteByUuid") // 修正了拼写并更新了路径
    public R<Boolean> batchDeleteByUuid(@RequestBody UuidParam uuidParam) {
        return R.ok(learningAnnotationsAndLabelsService.batchDeleteByUuid(uuidParam));
    }
    @ApiOperation(value = "检查服务端是否存在具有指定 clientUuid 的学习批注和标注数据(客户端用)")
    @PostMapping("/checkExistByUuid")
    public R<Boolean> checkExistByUuid(@RequestBody String clientUuid) {
        if (StringUtils.isEmpty(clientUuid)) {
            return R.ok(false);
        }

        // 查询数据库中是否存在该clientUuid的学习批注和标注
        LambdaQueryWrapper<LearningAnnotationsAndLabels> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningAnnotationsAndLabels::getClientUuid, clientUuid);

        boolean exists = learningAnnotationsAndLabelsService.count(queryWrapper) > 0;
        return R.ok(exists);
    }


    @ApiOperation(value = "新增或修改学习标注和批注")
    @PostMapping("/saveOrUpdateLabels")
    public R<Boolean> saveOrUpdateLabels(@RequestBody LearningAnnotationsAndLabels param) {
        return R.ok(learningAnnotationsAndLabelsService.saveOrUpdateLabels(param));
    }

    @ApiOperation(value = "查询批注和标注")
    @PostMapping("/selectLabels")
    public R<List<LearningAnnotationsAndLabels>> selectLabels(@RequestParam("textbokkId") Long textbookId) {
        return R.ok(learningAnnotationsAndLabelsService.selectLabels(textbookId));
    }

    @ApiOperation(value = "获取需要同步的批注ID列表（客户端用）")
    @GetMapping("/getNewAnnotationIdsForClient")
    public R<List<Long>> getNewAnnotationIdsForClient() {
        return R.ok(learningAnnotationsAndLabelsService.getNewAnnotationIdsForClient());
    }

    @ApiOperation(value = "根据ID列表获取批注完整数据（客户端用）")
    @PostMapping("/getAnnotationsByIds")
    public R<List<LearningAnnotationsAndLabels>> getAnnotationsByIds(@RequestBody List<Long> ids) {
        return R.ok(learningAnnotationsAndLabelsService.getAnnotationsByIds(ids));
    }

    @ApiOperation(value = "确认批注已同步（客户端用）")
    @PostMapping("/confirmAnnotationsSync")
    public R<Void> confirmAnnotationsSync(@RequestBody List<Long> ids) {
        boolean success = learningAnnotationsAndLabelsService.confirmAnnotationsSync(ids);
        return success ? R.ok() : R.fail("确认同步失败");
    }
}