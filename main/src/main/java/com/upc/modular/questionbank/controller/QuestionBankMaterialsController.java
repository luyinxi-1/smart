package com.upc.modular.questionbank.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.questionbank.controller.param.QuestionBankMaterialVO;
import com.upc.modular.questionbank.controller.param.QuestionBankMaterialsParam;
import com.upc.modular.questionbank.service.IQuestionBankMaterialsMappingService;
import com.upc.modular.questionbank.service.ITeachingQuestionBankService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.modular.questionbank.controller.param.QuestionBankMaterialsPageParam;
import com.upc.modular.questionbank.entity.QuestionBankMaterialsMapping;
import java.util.List;

/**
 * <p>
 * 题库素材管理控制器
 * </p>
 *
 * @author cyy
 * @since 2025-10-27
 */
@RestController
@RequestMapping("/question-bank-materials")
@Api(tags = "题库素材管理")
public class QuestionBankMaterialsController {

    @Autowired
    private IQuestionBankMaterialsMappingService questionBankMaterialsMappingService;

    @Autowired
    private ITeachingQuestionBankService teachingQuestionBankService;

    @ApiOperation("获取题库关联的素材列表")
    @GetMapping("/list/{questionBankId}")
    public R<List<QuestionBankMaterialVO>> getQuestionBankMaterialsList(
            @ApiParam(value = "题库ID", required = true) @PathVariable Long questionBankId) {
        List<QuestionBankMaterialVO> result = teachingQuestionBankService.getQuestionBankMaterials(questionBankId);
        return R.ok(result);
    }

    @ApiOperation("为题库添加素材")
    @PostMapping("/add")
    public R<Void> addMaterialsToQuestionBank(@RequestBody QuestionBankMaterialsParam param) {
        boolean success = questionBankMaterialsMappingService.batchAddMaterials(
                param.getQuestionBankId(),
                param.getMaterialIds()
        );
        if (success) {
            return R.commonReturn(200, "添加成功", null);
        } else {
            return R.commonReturn(500, "添加失败", null);
        }
    }

    @ApiOperation("更新题库的素材（完全替换）")
    @PostMapping("/update")
    public R<Void> updateQuestionBankMaterials(@RequestBody QuestionBankMaterialsParam param) {
        boolean success = questionBankMaterialsMappingService.updateMaterials(
                param.getQuestionBankId(),
                param.getMaterialIds()
        );
        if (success) {
            return R.commonReturn(200, "更新成功", null);
        } else {
            return R.commonReturn(500, "更新失败", null);
        }
    }

    @ApiOperation("删除题库的部分素材")
    @PostMapping("/remove")
    public R<Void> removeMaterialsFromQuestionBank(@RequestBody QuestionBankMaterialsParam param) {
        boolean success = questionBankMaterialsMappingService.batchRemoveMaterials(
                param.getQuestionBankId(),
                param.getMaterialIds()
        );
        if (success) {
            return R.commonReturn(200, "删除成功", null);
        } else {
            return R.commonReturn(500, "删除失败", null);
        }
    }

    @ApiOperation("分页查询题库素材列表")
    @PostMapping("/selectMaterialsPageList")
    public R<PageBaseReturnParam<QuestionBankMaterialsMapping>> selectMaterialsPageList(@RequestBody QuestionBankMaterialsPageParam param) {
        Page<QuestionBankMaterialsMapping> page = questionBankMaterialsMappingService.selectMaterialsPageList(param);
        PageBaseReturnParam<QuestionBankMaterialsMapping> p = PageBaseReturnParam.ok(page);
        return R.page(p);
    }
}

