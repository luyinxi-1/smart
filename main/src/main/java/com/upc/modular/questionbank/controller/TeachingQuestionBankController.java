package com.upc.modular.questionbank.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageSearchParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.service.ITeachingQuestionBankService;
import io.swagger.annotations.Api;
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
@RequestMapping("/teaching-question-bank")
@Api(tags = "题库")
public class TeachingQuestionBankController {
    @Autowired
    ITeachingQuestionBankService teachingQuestionBankService;
    @ApiOperation("新增题库")
    @PostMapping("/inserQuestionBank")
    public R inserQuestionBank(@RequestBody TeachingQuestionBank teachingQuestionbank){
        teachingQuestionBankService.inserQuestionBank(teachingQuestionbank);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation("删除题库")
    @PostMapping("deleteQuestionBank")
    public R deleteQuestionBank(@RequestBody IdParam idParam){
        teachingQuestionBankService.deleteQuestionBankByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation("更新题库信息")
    @PostMapping("updateQuestionBank")
    public R updateQuestionBank(@RequestBody TeachingQuestionBank teachingQuestionbank){
        teachingQuestionBankService.updateQuestionBank(teachingQuestionbank);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation("根据id查询单个题库信息")
    @PostMapping("selectQuestionBank")
    public R<TeachingQuestionBank> selectQuestionBank(@RequestBody TeachingQuestionBank teachingQuestionbank){
        TeachingQuestionBank result = teachingQuestionBankService.getById(teachingQuestionbank);
        return R.ok(result);
    }

    @ApiOperation("分页查询题库信息")
    @PostMapping("selectQuestionBankPage")
    public R<PageBaseReturnParam<TeachingQuestionBank>> selectQuestionBankPage(@RequestBody TeachingQuestionBankPageSearchParam param){
        Page<TeachingQuestionBank> page = teachingQuestionBankService.selectQuestionPage(param);
        PageBaseReturnParam<TeachingQuestionBank> p = PageBaseReturnParam.ok(page);
        return R.page(p);
    }

}
