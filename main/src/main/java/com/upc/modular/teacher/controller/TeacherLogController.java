package com.upc.modular.teacher.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageReturnParam;
import com.upc.modular.teacher.dto.TeacherLogPageSearchParam;
import com.upc.modular.teacher.entity.TeacherLog;
import com.upc.modular.teacher.service.ITeacherLogService;
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
 * @since 2025-08-21
 */
@RestController
@RequestMapping("/teacher-log")
@Api(tags = "教师日志")
public class TeacherLogController {

    @Autowired
    private ITeacherLogService teacherLogService;
    @ApiOperation("新增教师日志")
    @PostMapping("/inserTeacherLog")
    public R inserTeacherLog(@RequestBody TeacherLog param){
        teacherLogService.inserTeacherLog(param);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation("分页查询教师日志")
    @PostMapping("selectTeacherLogPage")
    public R<PageBaseReturnParam<TeacherLog>> selectTeacherLogPage(@RequestBody TeacherLogPageSearchParam param){
        Page<TeacherLog> page = teacherLogService.selectTeacherLogPage(param);
        PageBaseReturnParam<TeacherLog> p = PageBaseReturnParam.ok(page);
        return R.page(p);
    }
}
