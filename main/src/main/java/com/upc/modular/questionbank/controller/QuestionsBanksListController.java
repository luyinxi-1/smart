package com.upc.modular.questionbank.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.entity.QuestionsBanksList;
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

}
