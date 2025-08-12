package com.upc.modular.textbook.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.TextbookAuthority;
import com.upc.modular.textbook.param.TextbookAuthoritySearchParam;
import com.upc.modular.textbook.service.ITextbookAuthorityService;
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
 * @since 2025-07-08
 */
@RestController
@RequestMapping("/textbook-authority")
@Api(tags = "教材权限")
public class TextbookAuthorityController {

    @Autowired
    private ITextbookAuthorityService textbookAuthorityService;

    @ApiOperation(value = "删除教材权限信息")
    @PostMapping("/deleteTextbookAuthorityByIds")
    public R deleteTextbookAuthorityByIds(@RequestBody List<Long> ids) {
        textbookAuthorityService.deleteTextbookAuthorityByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "新增教材权限信息")
    @PostMapping("/insertTextbookAuthority")
    public R insertTextbookAuthority(@RequestBody TextbookAuthority textbookAuthority) {
        textbookAuthorityService.insertTextbookAuthority(textbookAuthority);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "查询教材权限信息")
    @PostMapping("/getTextbookAuthorityById")
    public R getTextbookAuthorityById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        TextbookAuthority textbookAuthority = textbookAuthorityService.getById(id);
        return R.commonReturn(200, "查询成功", textbookAuthority);
    }

//    @ApiOperation(value = "修改教材权限信息")
//    @PostMapping("/updateTextbookAuthorityById")
//    public R updateTextbookAuthorityById(@RequestBody TextbookAuthority textbookAuthority) {
//        textbookAuthorityService.updateTextbookAuthorityById(textbookAuthority);
//        return R.commonReturn(200, "修改成功", "");
//    }

    @ApiOperation(value = "按条件分页查询教材权限")
    @PostMapping("/getTextbookAuthorityPage")
    public R<Page<TextbookAuthority>> getTextbookAuthorityPage(@RequestBody TextbookAuthoritySearchParam param) {
        Page<TextbookAuthority> textbookAuthorityPage = textbookAuthorityService.getTextbookAuthorityPage(param);
        return R.ok(textbookAuthorityPage);
    }

    @ApiOperation(value = "判断指定用户是否有权限访问该教材")
    @PostMapping("/textbookAuthorityJudge")
    public R textbookAuthorityJudge(@RequestParam Long TextBookId, @RequestParam Long UserId) {
        return R.ok(textbookAuthorityService.textbookAuthorityJudge(TextBookId, UserId));
    }
}
