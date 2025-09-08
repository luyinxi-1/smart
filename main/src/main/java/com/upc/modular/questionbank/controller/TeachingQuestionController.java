package com.upc.modular.questionbank.controller;

import com.upc.modular.auth.entity.SysTbuser;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.course.entity.Course;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.service.ITeachingQuestionService;
import com.upc.modular.questionbank.controller.param.TeachingQuestionWithCreatorDto;
import com.upc.modular.teacher.service.ITeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private com.upc.modular.auth.mapper.SysUserMapper sysUserMapper;

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


/*    @ApiOperation("根据id查询单个题目信息")
    @PostMapping("selectQuestion")
    public R<TeachingQuestion> selectQuestion(@RequestBody TeachingQuestion teachingQuestion){
        TeachingQuestion result = teachingQuestionService.getById(teachingQuestion);
        return R.ok(result);
    }*/
/*@ApiOperation("根据id查询单个题目信息（含creatorName）")
@GetMapping("/selectQuestionById")
public R<TeachingQuestion> selectQuestionById(@RequestParam Long id) {
    TeachingQuestion result = teachingQuestionService.selectQuestionById(id);
    return R.ok(result);
}*/
@ApiOperation("根据id查询单个题目信息（含creatorName）")
@GetMapping("/selectQuestionById")
public R<TeachingQuestion> selectQuestionById(@RequestParam Long id) {
    TeachingQuestion result = teachingQuestionService.selectQuestionById(id);
    return R.ok(result);
}

    @ApiOperation("分页查询题目信息")
    @PostMapping("selectQuestionPage")
    public R<PageBaseReturnParam<TeachingQuestion>> selectQuestionPage(@RequestBody TeachingQuestionPageSearchParam teachingQuestion){
        Page<TeachingQuestion> page = teachingQuestionService.selectQuestionPage(teachingQuestion);
        PageBaseReturnParam<TeachingQuestion> p = PageBaseReturnParam.ok(page);
        return R.page(p);
    }
}
