package com.upc.modular.questionbank.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageReturnParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageSearchParam;
import com.upc.modular.questionbank.entity.StudentExercisesContent;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.service.IStudentExercisesContentService;
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
@RequestMapping("/student-exercises-content")
@Api(tags = "学生做题内容")
public class StudentExercisesContentController {
//    @Autowired
//    IStudentExercisesContentService studentExercisesContentService;
//    @ApiOperation("新增学生做题内容记录")
//    @PostMapping("/inserStudentExercisesContent")
//    public R inserStudentExercisesContent(@RequestBody StudentExercisesContent param){
//        studentExercisesContentService.inserStudentExercisesContent(param);
//        return R.commonReturn(200, "新增成功", "");
//    }
//
//    @ApiOperation("删除学生做题内容记录")
//    @PostMapping("deleteStudentExercisesContent")
//    public R deleteStudentExercisesContent(@RequestBody IdParam idParam){
//        studentExercisesContentService.deleteQuestionBankByIds(idParam);
//        return R.commonReturn(200, "删除成功", "");
//    }
//
//    @ApiOperation("更新学生做题内容记录")
//    @PostMapping("updateStudentExercisesContent")
//    public R updateStudentExercisesContent(@RequestBody TeachingQuestionBank teachingQuestionbank){
//        studentExercisesContentService.updateQuestionBank(teachingQuestionbank);
//        return R.commonReturn(200, "修改成功", "");
//    }
//
//    @ApiOperation("根据id查询单个学生做题内容记录")
//    @PostMapping("selectStudentExercisesContent")
//    public R<StudentExercisesContent> selectStudentExercisesContent(@RequestBody TeachingQuestionBank teachingQuestionbank){
//        TeachingQuestionBank result = studentExercisesContentService.getById(teachingQuestionbank);
//        return R.ok(result);
//    }
//
//    @ApiOperation("分页查询学生做题内容记录")
//    @PostMapping("selectStudentExercisesContent")
//    public R<PageBaseReturnParam<TeachingQuestionBankPageReturnParam>> selectQuestionBankPage(@RequestBody TeachingQuestionBankPageSearchParam param){
//        Page<TeachingQuestionBankPageReturnParam> page = studentExercisesContentService.selectQuestionPage(param);
//        PageBaseReturnParam<TeachingQuestionBankPageReturnParam> p = PageBaseReturnParam.ok(page);
//        return R.page(p);
//    }

}
