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

}
