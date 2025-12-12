package com.upc.modular.textbook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.StudentLearningLog;
import com.upc.modular.textbook.param.StudentLearningLogPageSearchParam;
import com.upc.modular.textbook.param.StudentLearningLogSaveParam;
import com.upc.modular.textbook.service.IStudentLearningLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student-learning-log")
@Api(tags = "学生学习日志管理")
public class StudentLearningLogController {

    @Autowired
    private IStudentLearningLogService studentLearningLogService;

    @ApiOperation("新增修改学习日志（学生）（传id时修改)")
    @PostMapping("/save")
    public R<Void> saveLog(@RequestBody StudentLearningLogSaveParam param) {
        return studentLearningLogService.saveLog(param);
    }

    @ApiOperation("提交学习日志（学生）")
    @PostMapping("/submit")
    public R<Void> submitLog(@RequestParam Long logId) {

        return studentLearningLogService.submitLog(logId);
    }

    @ApiOperation("获取学习日志列表（通用：学生查自己，教师查自己教材的。管理查所有）")
    @PostMapping("/page")
    public R<Page<StudentLearningLog>> getLogPage(@RequestBody StudentLearningLogPageSearchParam param) {
        Page<StudentLearningLog> page = studentLearningLogService.getLogPage(param);
        return R.ok(page);
    }


    @ApiOperation("查询日志详情")
    @GetMapping("/detail")
    public R<StudentLearningLog> getLogDetail(@RequestParam Long logId) {
        StudentLearningLog log = studentLearningLogService.getLogDetail(logId);
        return R.ok(log);
    }

    @ApiOperation("删除未提交的学习日志（学生）")
    @PostMapping("/delete")
    public R<Void> deleteUnsubmittedLog(@RequestParam Long logId) {
        return studentLearningLogService.deleteUnsubmittedLog(logId);
    }
}