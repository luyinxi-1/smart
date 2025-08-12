package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.TextbookClassification;
import com.upc.modular.textbook.param.TextbookClassificationSearchParam;
import com.upc.modular.textbook.param.TopLevelTextbookClassificationSearchParam;
import com.upc.modular.textbook.service.ITextbookClassificationService;
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
 * @since 2025-08-12
 */
@RestController
@RequestMapping("/textbook-classification")
@Api(tags = "教材分类管理")
public class TextbookClassificationController {

    @Autowired
    private ITextbookClassificationService textbookClassificationService;

    @PostMapping("/insertTextbookClassification")
    @ApiOperation("新增教材分类")
    public R<Void> insertTextbookClassification(@RequestBody TextbookClassification param){
        textbookClassificationService.insertTextbookClassification(param);
        return R.ok();
    }

    @PostMapping("/removeTextbookClassification")
    @ApiOperation("批量删除教材分类")
    public R<Void> removeTextbookClassification(@RequestParam List<Long> idList){
        textbookClassificationService.removeTextbookClassification(idList);
        return R.ok();
    }

    @PostMapping("/updateTextbookClassification")
    @ApiOperation("更新教材分类")
    public R<Boolean> updateTextbookClassification(@RequestBody TextbookClassification param){
        boolean result = textbookClassificationService.updateTextbookClassification(param);
        return R.ok(result);
    }

    @PostMapping("/selectTextbookClassificationParentIdList")
    @ApiOperation("获取教材分类的所有上级分类")
    public R<List<TextbookClassification>> selectTextbookClassificationParentIdList(@RequestParam Integer classificationGrade){
        List<TextbookClassification> list = textbookClassificationService.selectTextbookClassificationParentIdList(classificationGrade);
        return R.ok(list);
    }

    @PostMapping("/selectTextbookClassificationDownList")
    @ApiOperation("获取教材分类的下级分类")
    public R<List<TextbookClassification>> selectTextbookClassificationDownList(@RequestParam Long id){
        List<TextbookClassification> list = textbookClassificationService.selectTextbookClassificationDownList(id);
        return R.ok(list);
    }

    @PostMapping("/selectTextbookClassificationList")
    @ApiOperation("获取分类列表")
    public R<List<TextbookClassification>> selectTextbookClassificationList(@RequestBody TextbookClassificationSearchParam param){
        List<TextbookClassification> list = textbookClassificationService.selectTextbookClassificationList(param);
        List<TextbookClassification> ecoChainProductClassification = textbookClassificationService.buildDictTree(list);
        return R.ok(ecoChainProductClassification);
    }

    @PostMapping("/updateTextbookClassificationSortName")
    @ApiOperation("更改教材分类排序(0向上，1向下)")
    public R<Boolean> updateTextbookClassificationSortName(@RequestParam Long id, @RequestParam Integer param){
        boolean result = textbookClassificationService.updateTextbookClassificationSortName(id, param);
        return R.ok(result);
    }

    @PostMapping("/selectTopLevelTextbookClassification")
    @ApiOperation("查询顶级教材分类")
    public R<List<TextbookClassification>> selectTopLevelTextbookClassification(@RequestBody TopLevelTextbookClassificationSearchParam param){
        List<TextbookClassification> list = textbookClassificationService.selectTopLevelTextbookClassification(param);
        return R.ok(list);
    }
}
