package com.upc.modular.questionbank.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserUtils;
import com.upc.modular.questionbank.controller.param.SubmitAnswerRequest;
import com.upc.modular.questionbank.service.IStudentExercisesRecordService;
import com.upc.modular.student.mapper.StudentMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@RequestMapping("/student-exercises-record")
@Api(tags = "学生做题记录")
public class StudentExercisesRecordController {
    @Autowired
    private IStudentExercisesRecordService studentExercisesRecordService;

    @ApiOperation("学生提交答卷")
    @PostMapping("/submitQuestionBank")
    public R<Long> submitQuestionBank(@RequestBody SubmitAnswerRequest request) {
        Long userId = UserUtils.get().getId();
        Long recordId = studentExercisesRecordService.submitAnswers(userId, request);
        return R.commonReturn(200, "提交成功，已进入判卷流程", recordId);
    }

    @ApiOperation("手动触发最终成绩计算")
    @PostMapping("/recalculateFinalGrade")
    public R recalculateFinalGrade(@ApiParam(value = "已完成的答卷记录ID", required = true) @RequestParam Long recordId) {
        studentExercisesRecordService.calculateAndUpdateFinalGrade(recordId);
        return R.commonReturn(200, "最终成绩已重新计算", null);
    }

}
