package com.upc.modular.questionbank.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.QuestionsBanksListPageSearchParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageSearchParam;
import com.upc.modular.questionbank.entity.QuestionsBanksList;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.service.IQuestionsBanksListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@RestController
@RequestMapping("/questions-banks-list")
@Api(tags = "题目题库关联")
public class QuestionsBanksListController {
    @Autowired
    IQuestionsBanksListService questionsBanksListService;

    @ApiOperation("新增题目题库关联")
    @PostMapping("/insertQuestionsBanksList")
    public R insertQuestionsBanksList(@RequestBody QuestionsBanksList param){
        questionsBanksListService.inserQuestionBankList(param);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation("删除题目题库关联")
    @PostMapping("deleteQuestionsBanksList")
    public R deleteQuestionsBanksList(@RequestBody IdParam idParam){
        questionsBanksListService.deleteQuestionsBanksListByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation("更新题目题库关联信息")
    @PostMapping("updateQuestionsBanksList")
    public R updateQuestionsBanksList(@RequestBody QuestionsBanksList param){
        questionsBanksListService.updateQuestionsBanksList(param);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation("根据id查询单个题目题库关联信息")
    @PostMapping("selectQuestionBankList")
    public R<QuestionsBanksList> selectQuestionBankList(@RequestBody QuestionsBanksList param){
        QuestionsBanksList result = questionsBanksListService.getById(param);
        return R.ok(result);
    }

    @ApiOperation("分页查询题目题库关联信息")
    @PostMapping("selectQuestionBankListPage")
    public R<PageBaseReturnParam<QuestionsBanksList>> selectQuestionBankListPage(@RequestBody QuestionsBanksListPageSearchParam param){
        Page<QuestionsBanksList> page = questionsBanksListService.selectQuestionPageList(param);
        PageBaseReturnParam<QuestionsBanksList> p = PageBaseReturnParam.ok(page);
        return R.page(p);
    }



}
