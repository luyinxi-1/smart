package com.upc.modular.datastatistics.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.context.LoginContextHolder;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.entity.TeacherStatistics;
import com.upc.modular.datastatistics.service.ITeacherStatisticsService;
import com.upc.modular.teacher.service.ITeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 教师统计Controller
 */
@RestController
@RequestMapping("/teacher-statistics")
@Api(tags = "教师个人数据统计")
public class TeacherStatisticsController {

    @Autowired
    private ITeacherStatisticsService teacherStatisticsService;

    @Autowired
    private ITeacherService teacherService;

    @ApiOperation("获取教师个人数据统计")
    @PostMapping("/personal")
    public R<TeacherStatisticsReturnParam> getTeacherPersonalStatistics() {
        // 从当前登录用户获取教师ID
        UserInfoToRedis currentUser = LoginContextHolder.getUserInfoToRedis();
        if (currentUser == null || currentUser.getId() == null) {
            return R.fail("用户未登录");
        }
        
        Long teacherId = teacherService.getTeacherIdByUserId(currentUser.getId());
        if (teacherId == null) {
            return R.fail("当前用户不是教师");
        }
        
        return R.ok(teacherStatisticsService.getTeacherPersonalStatistics(teacherId));
    }

    @ApiOperation("统计教师授课班级数量")
    @PostMapping("/class-count")
    public R<Integer> countTeacherClasses() {
        // 从当前登录用户获取教师ID
        UserInfoToRedis currentUser = LoginContextHolder.getUserInfoToRedis();
        if (currentUser == null || currentUser.getId() == null) {
            return R.fail("用户未登录");
        }
        
        Long teacherId = teacherService.getTeacherIdByUserId(currentUser.getId());
        if (teacherId == null) {
            return R.fail("当前用户不是教师");
        }
        
        return R.ok(teacherStatisticsService.countTeacherClasses(teacherId));
    }

    @ApiOperation("统计教师授课学生数量")
    @PostMapping("/student-count")
    public R<Integer> countTeacherStudents() {
        // 从当前登录用户获取教师ID
        UserInfoToRedis currentUser = LoginContextHolder.getUserInfoToRedis();
        if (currentUser == null || currentUser.getId() == null) {
            return R.fail("用户未登录");
        }
        
        Long teacherId = teacherService.getTeacherIdByUserId(currentUser.getId());
        if (teacherId == null) {
            return R.fail("当前用户不是教师");
        }
        
        return R.ok(teacherStatisticsService.countTeacherStudents(teacherId));
    }

    @ApiOperation("统计教师教材数量")
    @PostMapping("/textbook-count")
    public R<Integer> countTeacherTextbooks() {
        // 从当前登录用户获取教师ID
        UserInfoToRedis currentUser = LoginContextHolder.getUserInfoToRedis();
        if (currentUser == null || currentUser.getId() == null) {
            return R.fail("用户未登录");
        }
        
        Long teacherId = teacherService.getTeacherIdByUserId(currentUser.getId());
        if (teacherId == null) {
            return R.fail("当前用户不是教师");
        }
        
        return R.ok(teacherStatisticsService.countTeacherTextbooks(teacherId));
    }

    @ApiOperation("统计教师授课课程数量")
    @PostMapping("/course-count")
    public R<Integer> countTeacherCourses() {
        // 从当前登录用户获取教师ID
        UserInfoToRedis currentUser = LoginContextHolder.getUserInfoToRedis();
        if (currentUser == null || currentUser.getId() == null) {
            return R.fail("用户未登录");
        }
        
        Long teacherId = teacherService.getTeacherIdByUserId(currentUser.getId());
        if (teacherId == null) {
            return R.fail("当前用户不是教师");
        }
        
        return R.ok(teacherStatisticsService.countTeacherCourses(teacherId));
    }

    @ApiOperation("获取教师教材热度排名")
    @GetMapping("/textbook-popularity")
    public R<IPage<TeacherTextbookPopularityParam>> getTeacherTextbookPopularity(Page<TeacherTextbookPopularityParam> page) {
        // 从当前登录用户获取教师ID
        UserInfoToRedis currentUser = LoginContextHolder.getUserInfoToRedis();
        if (currentUser == null || currentUser.getId() == null) {
            return R.fail("用户未登录");
        }

        Long teacherId = teacherService.getTeacherIdByUserId(currentUser.getId());
        if (teacherId == null) {
            return R.fail("当前用户不是教师");
        }

        return R.ok(teacherStatisticsService.getTeacherTextbookPopularity(page, teacherId));
    }

    @ApiOperation("导出教师教材热度排名")
    @GetMapping("/export-textbook-popularity")
    public void exportTeacherTextbookPopularity(HttpServletResponse response) throws IOException {
        // 从当前登录用户获取教师ID
        UserInfoToRedis currentUser = LoginContextHolder.getUserInfoToRedis();
        if (currentUser == null || currentUser.getId() == null) {
            // 处理错误，例如抛出异常或返回错误信息
            return;
        }

        Long teacherId = teacherService.getTeacherIdByUserId(currentUser.getId());
        if (teacherId == null) {
            // 处理错误
            return;
        }

        List<TeacherTextbookPopularityParam> list = teacherStatisticsService.exportTeacherTextbookPopularity(teacherId);

        String fileName = "教师教材热度排名.xlsx";
        String fallbackName = "teacher_popularity_report.xlsx"; // 纯英文备用文件名

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        String contentDisposition = String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", fallbackName, encodedFileName);
        response.setHeader("Content-Disposition", contentDisposition);

        EasyExcel.write(response.getOutputStream(), TeacherTextbookPopularityParam.class).sheet("排名").doWrite(list);
    }

    @ApiOperation("保存教师统计数据")
    @PostMapping("/save")
    public R<Void> saveTeacherStatistics(@RequestBody TeacherStatistics statistics) {
        teacherStatisticsService.saveTeacherStatistics(statistics);
        return R.ok();
    }

    // ========== 智能化分析功能API ==========

    @ApiOperation("班级章节掌握情况分析")
    @GetMapping("/class-chapter-mastery")
    public R<ClassChapterMasteryReturnParam> analyzeClassChapterMastery(
            @ApiParam(value = "班级ID", required = true) @RequestParam Long classId,
            @ApiParam(value = "教材ID", required = true) @RequestParam Long textbookId) {
        try {
            ClassChapterMasteryReturnParam result = teacherStatisticsService.analyzeClassChapterMastery(classId, textbookId);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("班级章节掌握情况分析失败: " + e.getMessage());
        }
    }

    @ApiOperation("班级分析报告")
    @GetMapping("/class-analysis-report")
    public R<ClassAnalysisReturnParam> generateClassAnalysisReport(
            @ApiParam(value = "班级ID", required = true) @RequestParam Long classId,
            @ApiParam(value = "开始时间", required = true) @RequestParam String startTime,
            @ApiParam(value = "结束时间", required = true) @RequestParam String endTime) {
        try {
            ClassAnalysisReturnParam result = teacherStatisticsService.generateClassAnalysisReport(classId, startTime, endTime);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("班级分析报告生成失败: " + e.getMessage());
        }
    }

    @ApiOperation("班级学习行为分析")
    @GetMapping("/class-behavior-analysis")
    public R<ClassBehaviorAnalysisReturnParam> analyzeClassLearningBehavior(
            @ApiParam(value = "班级ID", required = true) @RequestParam Long classId,
            @ApiParam(value = "开始时间", required = true) @RequestParam String startTime,
            @ApiParam(value = "结束时间", required = true) @RequestParam String endTime) {
        try {
            ClassBehaviorAnalysisReturnParam result = teacherStatisticsService.analyzeClassLearningBehavior(classId, startTime, endTime);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("班级学习行为分析失败: " + e.getMessage());
        }
    }
}