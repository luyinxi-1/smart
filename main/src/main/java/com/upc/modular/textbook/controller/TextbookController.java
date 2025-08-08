package com.upc.modular.textbook.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
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
 * @since 2025-07-08
 */
@RestController
@RequestMapping("/textbook")
@Api(tags = "教材管理")
public class TextbookController {
    @Autowired
    private ITextbookService textbookService;

    @ApiOperation(value = "新增教材")
    @PostMapping("/insert")
    public R insert(@RequestBody Textbook textbook) {
        textbookService.insert(textbook);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "删除教材")
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        textbookService.deleteDictItemByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改教材")
    @PostMapping("/update")
    public R update(@RequestBody Textbook textbook) {
        textbookService.updateTextbook(textbook);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "分页查询教材")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<TextbookPageReturnParam>> getPage(@RequestBody TextbookPageSearchParam param) {
        Page<TextbookPageReturnParam> page = textbookService.getPage(param);
        PageBaseReturnParam<TextbookPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查询最新的教材")
    @PostMapping("/getNewTextbook")
    public R<List<Textbook>> getNewTextbook(@RequestParam("getNumber") int getNumber) {
        return R.ok(textbookService.getNewTextbook(getNumber));
    }

    @ApiOperation(value = "查询单本教材详情")
    @PostMapping("/getOneTextbookDetails")
    public R<TextbookPageReturnParam> getOneTextbookDetails(@RequestParam("textbookId") Long textbookId) {
        return R.ok(textbookService.getOneTextbookDetails(textbookId));
    }
}
