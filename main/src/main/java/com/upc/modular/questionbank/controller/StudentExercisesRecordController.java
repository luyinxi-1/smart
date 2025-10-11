package com.upc.modular.questionbank.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.StudentExercisesContentPageSearchParam;
import com.upc.modular.questionbank.controller.param.StudentExercisesRecordPageSearchParam;
import com.upc.modular.questionbank.controller.param.SubmitAnswerRequest;
import com.upc.modular.questionbank.entity.StudentExercisesContent;
import com.upc.modular.questionbank.entity.StudentExercisesRecord;
import com.upc.modular.questionbank.service.IStudentExercisesRecordService;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Autowired
    private StudentMapper studentMapper;

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
    public R<List<StudentExercisesRecord>> selectStudentExercisesRecord(@RequestBody StudentExercisesRecord param){
        List<StudentExercisesRecord> resultList = studentExercisesRecordService.list(
                new QueryWrapper<StudentExercisesRecord>()
                        .eq("student_id", param.getStudentId())
                        .eq("teaching_question_bank_id", param.getTeachingQuestionBankId())
        );
        return R.ok(resultList);
    }

    @ApiOperation("APP专用-根据题库ID查询当前学生做题记录")
    @PostMapping("selectMyStudentExercisesRecord")
    public R<List<StudentExercisesRecord>> selectMyStudentExercisesRecord(@RequestParam Long teachingQuestionBankId){
        // 1. 获取当前登录用户ID
        if (ObjectUtils.isEmpty(UserUtils.get()) || ObjectUtils.isEmpty(UserUtils.get().getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }
        Long userId = UserUtils.get().getId();

        // 2. 根据用户ID查询学生主键ID
        LambdaQueryWrapper<Student> studentQueryWrapper = new LambdaQueryWrapper<>();
        studentQueryWrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(studentQueryWrapper);

        if (student == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "当前用户不是学生或学生信息不存在");
        }

        Long studentId = student.getId();

        // 3. 参数校验
        if (teachingQuestionBankId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "题库ID不能为空");
        }

        // 4. 根据题库ID和学生ID查询做题记录
        LambdaQueryWrapper<StudentExercisesRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentExercisesRecord::getTeachingQuestionBankId, teachingQuestionBankId)
                .eq(StudentExercisesRecord::getStudentId, studentId)
                .orderByDesc(StudentExercisesRecord::getAddDatetime); // 按创建时间倒序排列

        List<StudentExercisesRecord> result = studentExercisesRecordService.list(queryWrapper);
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
