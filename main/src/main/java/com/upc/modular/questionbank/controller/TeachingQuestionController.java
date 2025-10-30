package com.upc.modular.questionbank.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.QuestionCountByTypeReturnParam;
import com.upc.modular.questionbank.controller.param.QuestionCountSearchParam;
import com.upc.modular.questionbank.controller.param.SmartPaperGenerationParam;
import com.upc.modular.questionbank.controller.param.SmartPaperQuestionVO;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchReturnVO;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.service.ITeachingQuestionService;
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
 * @since 2025-07-04
 */
@RestController
@RequestMapping("/teaching-question")
@Api(tags = "题目")
public class TeachingQuestionController {
    @Autowired
    ITeachingQuestionService teachingQuestionService;

    @ApiOperation("新增题目")
    @PostMapping("/inserQuestion")
    public R inserQuestion(@RequestBody TeachingQuestion teachingQuestion){
        teachingQuestionService.save(teachingQuestion);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation("删除题目")
    @PostMapping("deleteQuestion")
    public R deleteQuestion(@RequestBody IdParam idParam){
        teachingQuestionService.deleteCourseByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation("更新题目信息")
    @PostMapping("updateQuestion")
    public R updateQuestion(@RequestBody TeachingQuestion teachingQuestion){
        teachingQuestionService.updateById(teachingQuestion);
        return R.commonReturn(200, "修改成功", "");
    }

@ApiOperation("根据id查询单个题目信息（含creatorName）")
@GetMapping("/selectQuestionById")
public R<TeachingQuestion> selectQuestionById(@RequestParam Long id) {
    TeachingQuestion result = teachingQuestionService.selectQuestionById(id);
    return R.ok(result);
}

    @ApiOperation("分页查询题目信息")
    @PostMapping("selectQuestionPage")
    public R<PageBaseReturnParam<TeachingQuestionPageSearchReturnVO>> selectQuestionPage(@RequestBody TeachingQuestionPageSearchParam teachingQuestion){
        Page<TeachingQuestionPageSearchReturnVO> page = teachingQuestionService.selectQuestionPage(teachingQuestion);
        PageBaseReturnParam<TeachingQuestionPageSearchReturnVO> p = PageBaseReturnParam.ok(page);
        return R.page(p);
    }

    @ApiOperation("智能组卷（教师）")
    @PostMapping("/smartPaperGeneration")
    public R<List<SmartPaperQuestionVO>> smartPaperGeneration(@RequestBody SmartPaperGenerationParam param) {
        List<SmartPaperQuestionVO> result = teachingQuestionService.smartPaperGeneration(param);
        return R.ok(result);
    }
    
    @ApiOperation("获取题目数量（教师）")
    @PostMapping("/countQuestionsByType")
    public R<List<QuestionCountByTypeReturnParam>> countQuestionsByType(@RequestBody QuestionCountSearchParam param) {
        List<QuestionCountByTypeReturnParam> result = teachingQuestionService.countQuestionsByType(param.getTextbookId(), param.getChapterId());
        return R.ok(result);
    }
}