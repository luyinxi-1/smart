package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.param.TextbookCatalogDto;
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
    public R<Boolean> insert(@RequestBody TextbookCatalog param) {
        return R.ok(textbookCatalogService.insert(param));
    }

    @ApiOperation(value = "删除教材章节内容")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestParam Long id) {
        return R.ok(textbookCatalogService.delete(id));
    }

    @ApiOperation(value = "更新教材章节内容")
    @PutMapping("/update")
    public R<Boolean> update(@RequestBody TextbookCatalog param) {
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
    public R<List<TextbookCatalog>> readTextbook(@RequestParam Long id) {
        List<TextbookCatalog> result = textbookCatalogService.readTextbook(id);
        return R.ok(result);
    }

}
