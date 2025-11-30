package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.param.*;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    public R processAndSaveHtml(@RequestParam MultipartFile file, Long textbookId) {
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
    public void exportTextbook(HttpServletRequest request, HttpServletResponse response, @RequestParam("textbookId") Long textbookId) {
        String baseUrl = request.getScheme() + "://"   // http 或 https
                + request.getServerName()              // 域名 / IP
                + ":" + request.getServerPort();       // 端口
        textbookCatalogService.exportTextbook(response, textbookId, baseUrl);
    }

    @ApiOperation(value = "导出教材为PDF（id）")
    @PostMapping("/exportTextbookToPdfById")
    public void exportTextbookToPdf(HttpServletRequest request, HttpServletResponse response, @RequestParam("textbookId") Long textbookId) {
        String baseUrl = request.getScheme() + "://"   // http 或 https
                + request.getServerName()              // 域名 / IP
                + ":" + request.getServerPort();       // 端口
        textbookCatalogService.exportTextbookToPdf(response, textbookId, baseUrl);
    }

    @ApiOperation(value = "导出教材（html）")
    @PostMapping("/exportTextbookByString")
    public void exportTextbookByString(HttpServletResponse response, @RequestParam("html") String html) {
        textbookCatalogService.exportTextbookByString(response, html);
    }

    @ApiOperation(value = "查看教材")
    @PostMapping("/readTextbook")
    public R<List<ReadTextbookReturnParam>> readTextbook(@RequestParam("textbookId") Long textbookId) {
        List<ReadTextbookReturnParam> result = textbookCatalogService.readTextbook(textbookId);
        return R.ok(result);
    }
    @ApiOperation(value = "查看教材指定目录")
    @PostMapping("/readTextbookCatalog")
    public R<List<ReadTextbookReturnParam>> readTextbookCatalog(
            @RequestParam("textbookId") Long textbookId,
            @RequestParam("catalogId") Long catalogId) {
        List<ReadTextbookReturnParam> result = textbookCatalogService.readTextbookCatalog(textbookId, catalogId);
        return R.ok(result);
    }


    @ApiOperation(value = "返回教材目录树")
    @PostMapping("/getTextbookCatalogTree")
    public R<List<TextbookTree>> getTextbookCatalogTree(@RequestParam("textbookId") Long textbookId) {
        List<TextbookTree> result = textbookCatalogService.getTextbookCatalogTree(textbookId);
        return R.ok(result);
    }

    @ApiOperation(value = "返回指定级别目录")
    @PostMapping("/getTextbookSpecifiedCatalog")
    public R<List<Long>> getTextbookSpecifiedCatalog(@RequestBody TextbookSpecifiedCatalogSearchParam param) {
        List<Long> result = textbookCatalogService.getTextbookSpecifiedCatalog(param);
        return R.ok(result);
    }

    @ApiOperation(value = "下载教材目录")
    @GetMapping("/downloadTextbookCatalog")
    public R<List<TextbookCatalog>> downloadTextbookCatalog(@RequestParam("textbookId") Long textbookId) {
        List<TextbookCatalog> result = textbookCatalogService.downloadTextbookCatalog(textbookId);
        return R.ok(result);
    }
    
    @ApiOperation(value = "根据教材id查询素材分类及数量")
    @GetMapping("/getMaterialTypeCountByTextbookId")
    public R<List<MaterialTypeCountReturnParam>> getMaterialTypeCountByTextbookId(@RequestParam("textbookId") Long textbookId) {
        List<MaterialTypeCountReturnParam> result = textbookCatalogService.getMaterialTypeCountByTextbookId(textbookId);
        return R.ok(result);
    }

}