package com.upc.modular.course.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.course.controller.param.ClassInfoReturnParam;
import com.upc.modular.course.controller.param.CourseClassAssociateParam;
import com.upc.modular.course.service.ICourseClassListService;
import com.upc.modular.course.service.ICourseService;
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
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/course-class-list")
@Api(tags = "课程-班级管理模块")
public class CourseClassListController {
    @Autowired
    ICourseClassListService courseClassListService;

    @ApiOperation("给课程批量关联班级")
    @PostMapping("/associateClasses")
    public R<Void> associateClasses(@RequestBody CourseClassAssociateParam param) {
        courseClassListService.associateClasses(param.getCourseId(), param.getClassIdList());
        return R.ok();
    }

    @ApiOperation("查询某课程已关联的班级详情")
    @GetMapping("/associatedClass")
    public R<List<ClassInfoReturnParam>> associatedClass(@RequestParam Long courseId) {
        List<ClassInfoReturnParam> list = courseClassListService.getClassesByCourse(courseId);
        return R.ok(list);
    }

}
