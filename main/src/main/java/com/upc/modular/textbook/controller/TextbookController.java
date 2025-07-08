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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @ApiModelProperty("新增教材")
    @PostMapping("/insert")
    public R insert(@RequestBody Textbook textbook) {
        textbookService.insert(textbook);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiModelProperty("删除教材")
    @DeleteMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        textbookService.deleteDictItemByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiModelProperty("修改教材")
    @PutMapping("/update")
    public R update(@RequestBody Textbook textbook) {
        textbookService.updateTextbook(textbook);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiModelProperty("分页查询教材")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<TextbookPageReturnParam>> getPage(@RequestBody TextbookPageSearchParam param) {
        Page<TextbookPageReturnParam> page = textbookService.getPage(param);
        PageBaseReturnParam<TextbookPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
}
