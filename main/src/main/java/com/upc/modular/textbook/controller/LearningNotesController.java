package com.upc.modular.textbook.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.entity.LearningNotes;
import com.upc.modular.textbook.param.LearningNotesPageReturnParam;
import com.upc.modular.textbook.param.LearningNotesPageSearchParam;
import com.upc.modular.textbook.param.UuidParam;
import com.upc.modular.textbook.service.ILearningLogService;
import com.upc.modular.textbook.service.ILearningNotesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
@RestController
@RequestMapping("/learning-notes")
@Api(tags = "教学笔记")
public class LearningNotesController {
    @Autowired
    private ILearningNotesService learningNotesService;

    @ApiOperation(value = "新增学习笔记")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody LearningNotes learningNotes) {
        return R.ok(learningNotesService.insert(learningNotes));
    }

    @ApiOperation(value = "删除学习笔记")
    @PostMapping("batchDelete")
    public R<Boolean> batchDelete(@RequestBody IdParam idParam) {
        return R.ok(learningNotesService.batchDelete(idParam));
    }

    @ApiOperation(value = "根据UUID批量删除学习笔记（客户端删除使用)")
    @PostMapping("batchDeleteByUuid")
    public R<Boolean> batchDeleteByUuid(@RequestBody UuidParam uuidParam) {
        return R.ok(learningNotesService.batchDeleteByUuid(uuidParam));
    }
    @ApiOperation(value = "检查服务端是否存在具有指定 clientUuid 的学习批注和标注数据(客户端用)")
    @PostMapping("/checkExistByUuid")
    public R<Boolean> checkExistByUuid(@RequestBody String clientUuid) {
        if (StringUtils.isEmpty(clientUuid)) {
            return R.ok(false);
        }
        // 查询数据库中是否存在该clientUuid的学习笔记
        LambdaQueryWrapper<LearningNotes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningNotes::getClientUuid, clientUuid);

        boolean exists = learningNotesService.count(queryWrapper) > 0;
        return R.ok(exists);
    }

    @ApiOperation(value = "更新学习笔记")
    @PostMapping("/updateNotes")
    public R<Boolean> updateNotes(@RequestBody LearningNotes param) {
        return R.ok(learningNotesService.updateNotes(param));
    }

    @ApiOperation(value = "根据clientUuid更新学习笔记(客户端使用)")
    @PostMapping("/updateNotesbyUUID")
    public R<Boolean> updateNotesbyUUID(@RequestBody LearningNotes param) {
        return R.ok(learningNotesService.updateNotesbyClientUuid(param));
    }

    @ApiOperation(value = "查看笔记")
    @PostMapping("/getOneNote")
    public R<LearningNotes> getOneNote(@RequestParam("noteId") Long id) {
        return R.ok(learningNotesService.getOneNote(id));
    }

    @ApiOperation(value = "根据clientUuid查看笔记")
    @PostMapping("/getOneNoteByClientUuid")
    public R<LearningNotes> getOneNoteByClientUuid(@RequestParam("clientUuid") String clientUuid) {
        return R.ok(learningNotesService.getOneNoteByClientUuid(clientUuid));
    }

    @ApiOperation(value = "分页查询学习笔记（全部）")
    @PostMapping("/getAllPage")
    public R<PageBaseReturnParam<LearningNotesPageReturnParam>> getAllPage(@RequestBody LearningNotesPageSearchParam param) {
        Page<LearningNotesPageReturnParam> page = learningNotesService.getAllPage(param);
        PageBaseReturnParam<LearningNotesPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "分页查询学习笔记（个人）")
    @PostMapping("/getAMyPage")
    public R<PageBaseReturnParam<LearningNotesPageReturnParam>> getMyPage(@RequestBody LearningNotesPageSearchParam param) {
        Page<LearningNotesPageReturnParam> page = learningNotesService.getMyPage(param);
        PageBaseReturnParam<LearningNotesPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查询我有笔记的教材列表")
    @PostMapping("/getMyNotesTextbookCenter")
    public R<PageBaseReturnParam<LearningNotesPageReturnParam>> getMyNotesTextbookCenter(@RequestBody LearningNotesPageSearchParam param) {
        Page<LearningNotesPageReturnParam> page = learningNotesService.getMyNotesTextbookCenter(param);
        PageBaseReturnParam<LearningNotesPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
}