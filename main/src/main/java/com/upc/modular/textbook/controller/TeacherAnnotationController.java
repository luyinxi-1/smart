package com.upc.modular.textbook.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.TeacherAnnotation;
import com.upc.modular.textbook.param.TeacherAnnotationPageSearchParam;
import com.upc.modular.textbook.param.TeacherAnnotationReturnParam;
import com.upc.modular.textbook.service.ITeacherAnnotationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author mjh
 * @since 2025-09-02
 */
@RestController
@RequestMapping("/teacher-annotation")
@Api(tags = "教师批注管理")
public class TeacherAnnotationController {

    @Autowired
    private ITeacherAnnotationService teacherAnnotationService;

    @ApiOperation(value = "教师批注分页查询-by textbookId")
    @PostMapping("/get-teacher-annotation-page")
    public R<PageBaseReturnParam<TeacherAnnotationReturnParam>> getTeacherAnnotationPage(@RequestBody TeacherAnnotationPageSearchParam param) {
        Page<TeacherAnnotationReturnParam> result = teacherAnnotationService.getTeacherAnnotationPage(param);
        PageBaseReturnParam<TeacherAnnotationReturnParam> pageBaseReturnParam = PageBaseReturnParam.ok(result);
        return R.page(pageBaseReturnParam);
    }

    @ApiOperation(value = "添加教师批注")
    @PostMapping("/insert-teacher-annotation")
    public R<Long> insertTeacherAnnotation(@RequestBody TeacherAnnotation teacherAnnotation) {
        Long id = teacherAnnotationService.insertTeacherAnnotation(teacherAnnotation);
        return R.ok(id);
    }

    @ApiOperation(value = "查看教师批注")
    @PostMapping("/get-teacher-annotation")
    public R<TeacherAnnotationReturnParam> getTeacherAnnotation(@RequestParam Long id) {
        TeacherAnnotationReturnParam teacherAnnotation = teacherAnnotationService.getTeacherAnnotation(id);
        return R.ok(teacherAnnotation);
    }

    @ApiOperation(value = "删除教师批注")
    @PostMapping("/delete-teacher-annotation")
    public R<Boolean> deleteTeacherAnnotation(@RequestParam Long id) {
        boolean result = teacherAnnotationService.removeById(id);
        return R.ok(result);
    }

    @ApiOperation(value = "修改教师批注")
    @PostMapping("/update-teacher-annotation")
    public R<Boolean> updateTeacherAnnotation(@RequestBody TeacherAnnotation teacherAnnotation) {
        boolean result = teacherAnnotationService.updateById(teacherAnnotation);
        return R.ok(result);
    }

}
