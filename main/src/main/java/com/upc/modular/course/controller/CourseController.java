package com.upc.modular.course.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.course.controller.param.CoursePageReturnParam;
import com.upc.modular.course.controller.param.CoursePageSearchParam;
import com.upc.modular.course.controller.param.GetMyCourseReturnParam;
import com.upc.modular.course.entity.Course;
import com.upc.modular.course.service.ICourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
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
@RequestMapping("/course")
@Api(tags = "课程模块")
@Slf4j
public class CourseController {
    @Autowired
    ICourseService courseService;

//    @ApiOperation("新增课程信息")
//    @PostMapping("/inserCourse")
//    public R inserCourse(@RequestBody Course course){
//        courseService.save(course);
//        return R.commonReturn(200, "新增成功", "");
//    }

    @ApiOperation("新增课程信息")
    @PostMapping("/inserCourse")
    public R<Long> inserCourse(@RequestBody Course course){
        Long courseId = courseService.inserCourse(course);
        return R.ok(courseId);
    }

    @ApiOperation("删除课程信息")
    @PostMapping("deleteCourse")
    public R deleteCourse(@RequestBody IdParam idParam){
        courseService.deleteCourseByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation("更新课程信息")
    @PostMapping("updateCourse")
    public R updateCourse(@RequestBody Course course){
        courseService.updateById(course);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation("根据id查询单个课程信息")
    @PostMapping("selectCourse")
    public R<Course> selectCourse(@RequestBody Course course){
        Course result = courseService.getById(course);
        return R.ok(result);
    }

    @ApiOperation(value = "根据课程名称、教师姓名分页查询课程")
    @PostMapping("/selectCourseByPage")
    public R<PageBaseReturnParam<CoursePageReturnParam>> selectCourseByPage(@RequestBody CoursePageSearchParam param) {
        Page<CoursePageReturnParam> page = courseService.getPage(param);
        PageBaseReturnParam<CoursePageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation("导出课程信息")
    @PostMapping("/exportCourseData")
    public void exportLikeData(HttpServletResponse response, @RequestBody IdParam param){
        courseService.exportCourseData(response,param);
    }
    
    @ApiOperation("导出课程信息docx文档")
    @GetMapping("/exportCourseInfoDocx")
    public void exportCourseInfoDocx(HttpServletResponse response,
                                 @RequestParam Long courseId,
                                 @RequestParam(value = "classId", required = false) Long classId) {
        if (courseId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "课程ID不能为空");
        }
        courseService.exportCourseInfoDocx(response, courseId, classId);
    }
}
