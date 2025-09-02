package com.upc.modular.datastatistics.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.TeacherStatisticsReturnParam;
import com.upc.modular.datastatistics.entity.TeacherStatistics;
import com.upc.modular.datastatistics.service.ITeacherStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 教师统计Controller
 */
@RestController
@RequestMapping("/teacher-statistics")
@Api(tags = "教师统计")
public class TeacherStatisticsController {

    @Autowired
    private ITeacherStatisticsService teacherStatisticsService;

    @ApiOperation("获取教师个人数据统计")
    @PostMapping("/personal/{teacherId}")
    public R<TeacherStatisticsReturnParam> getTeacherPersonalStatistics(@PathVariable Long teacherId) {
        return R.ok(teacherStatisticsService.getTeacherPersonalStatistics(teacherId));
    }

    @ApiOperation("统计教师授课班级数量")
    @PostMapping("/class-count/{teacherId}")
    public R<Integer> countTeacherClasses(@PathVariable Long teacherId) {
        return R.ok(teacherStatisticsService.countTeacherClasses(teacherId));
    }

    @ApiOperation("统计教师授课学生数量")
    @PostMapping("/student-count/{teacherId}")
    public R<Integer> countTeacherStudents(@PathVariable Long teacherId) {
        return R.ok(teacherStatisticsService.countTeacherStudents(teacherId));
    }

    @ApiOperation("统计教师教材数量")
    @PostMapping("/textbook-count/{teacherId}")
    public R<Integer> countTeacherTextbooks(@PathVariable Long teacherId) {
        return R.ok(teacherStatisticsService.countTeacherTextbooks(teacherId));
    }

    @ApiOperation("统计教师授课课程数量")
    @PostMapping("/course-count/{teacherId}")
    public R<Integer> countTeacherCourses(@PathVariable Long teacherId) {
        return R.ok(teacherStatisticsService.countTeacherCourses(teacherId));
    }


    @ApiOperation("保存教师统计数据")
    @PostMapping("/save")
    public R<Void> saveTeacherStatistics(@RequestBody TeacherStatistics statistics) {
        teacherStatisticsService.saveTeacherStatistics(statistics);
        return R.ok();
    }
}