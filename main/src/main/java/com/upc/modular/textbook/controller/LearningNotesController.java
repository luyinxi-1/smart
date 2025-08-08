package com.upc.modular.textbook.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningNotes;
import com.upc.modular.textbook.param.LearningNotesPageReturnParam;
import com.upc.modular.textbook.param.LearningNotesPageSearchParam;
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

    @ApiOperation(value = "更新学习笔记")
    @PostMapping("/updateNotes")
    public R<Boolean> updateNotes(@RequestBody LearningNotes param) {
        return R.ok(learningNotesService.updateNotes(param));
    }

    @ApiOperation(value = "查看笔记")
    @PostMapping("/getOneNote")
    public R<LearningNotes> getOneNote(@RequestParam("noteId") Long id) {
        return R.ok(learningNotesService.getOneNote(id));
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
}
