package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.param.TextbookCatalogDto;
import com.upc.modular.textbook.param.TextbookCatalogInsertParam;
import com.upc.modular.textbook.param.TextbookTree;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
@RestController
@RequestMapping("/textbook-catalog")
@Api(tags = "教材目录")
public class TextbookCatalogController {

    @Resource
    private ITextbookCatalogService textbookCatalogService;

    @ApiOperation(value = "解析docx文档到数据库")
    @PostMapping("/processAndSaveHtml")
    public R processAndSaveHtml(@RequestParam MultipartFile file, @RequestParam Long textbookId) {
        textbookCatalogService.processAndSaveHtml(file, textbookId);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "插入教材章节内容")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody List<TextbookCatalogInsertParam> params) {
        return R.ok(textbookCatalogService.insert(params));
    }

    @ApiOperation(value = "删除教材章节内容")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody IdParam idParam) {
        return R.ok(textbookCatalogService.delete(idParam));
    }

    @ApiOperation(value = "更新教材章节内容")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody List<TextbookCatalog> param) {
        return R.ok(textbookCatalogService.updateTextbook(param));
    }

    @ApiOperation(value = "导出教材（id）")
    @PostMapping("/exportTextbookById")
    public void exportTextbook(HttpServletResponse response, @RequestParam("textbookId") Long textbookId) {
        textbookCatalogService.exportTextbook(response, textbookId);
    }

    @ApiOperation(value = "导出教材（html）")
    @PostMapping("/exportTextbookByString")
    public void exportTextbookByString(HttpServletResponse response, @RequestParam("html") String html) {
        textbookCatalogService.exportTextbookByString(response, html);
    }

    @ApiOperation(value = "查看教材")
    @PostMapping("/readTextbook")
    public R<List<TextbookCatalog>> readTextbook(@RequestParam("textbookId") Long textbookId) {
        List<TextbookCatalog> result = textbookCatalogService.readTextbook(textbookId);
        return R.ok(result);
    }

    @ApiOperation(value = "返回教材目录树")
    @PostMapping("/getTextbookCatalogTree")
    public R<List<TextbookTree>> getTextbookCatalogTree(@RequestParam("textbookId") Long textbookId) {
        List<TextbookTree> result = textbookCatalogService.getTextbookCatalogTree(textbookId);
        return R.ok(result);
    }

}
