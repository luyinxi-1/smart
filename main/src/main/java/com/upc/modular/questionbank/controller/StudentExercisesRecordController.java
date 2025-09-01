package com.upc.modular.questionbank.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserUtils;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.StudentExercisesContentPageSearchParam;
import com.upc.modular.questionbank.controller.param.StudentExercisesRecordPageSearchParam;
import com.upc.modular.questionbank.controller.param.SubmitAnswerRequest;
import com.upc.modular.questionbank.entity.StudentExercisesContent;
import com.upc.modular.questionbank.entity.StudentExercisesRecord;
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

    // 这个接口里面包含了新增做题记录的操作
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

    @ApiOperation("新增学生做题记录")
    @PostMapping("/inserStudentExercisesRecord")
    public R inserStudentExercisesRecord(@RequestBody StudentExercisesRecord param){
        studentExercisesRecordService.inserStudentExercisesRecord(param);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation("删除学生做题记录")
    @PostMapping("deleteStudentExercisesRecord")
    public R deleteStudentExercisesRecord(@RequestBody IdParam idParam){
        studentExercisesRecordService.deleteStudentExercisesRecordByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation("更新学生做题记录")
    @PostMapping("updateStudentExercisesRecord")
    public R updateStudentExercisesRecord(@RequestBody StudentExercisesRecord param){
        studentExercisesRecordService.updateStudentExercisesRecord(param);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation("根据id查询单个学生做题记录")
    @PostMapping("selectStudentExercisesRecord")
    public R<StudentExercisesRecord> selectStudentExercisesRecord(@RequestBody StudentExercisesRecord param){
        StudentExercisesRecord result = studentExercisesRecordService.getById(param);
        return R.ok(result);
    }

    @ApiOperation("分页查询学生做题记录")
    @PostMapping("selectStudentExercisesRecordPage")
    public R<PageBaseReturnParam<StudentExercisesRecord>> selectStudentExercisesRecordPage(@RequestBody StudentExercisesRecordPageSearchParam param){
        Page<StudentExercisesRecord> page = studentExercisesRecordService.selectStudentExercisesRecordPage(param);
        PageBaseReturnParam<StudentExercisesRecord> p = PageBaseReturnParam.ok(page);
        return R.page(p);
    }
}
