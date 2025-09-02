package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.TeacherAnnotation;
import com.upc.modular.textbook.param.TeacherAnnotationReturnParam;
import com.upc.modular.textbook.service.ITeacherAnnotationService;
import com.upc.modular.textbook.service.impl.TeacherAnnotationServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author mjh
 * @since 2025-09-02
 */
@RestController
@RequestMapping("/teacher-annotation")
public class TeacherAnnotationController {

    @Autowired
    private ITeacherAnnotationService teacherAnnotationService;

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

}
