package com.upc.modular.questionbank.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserUtils;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.*;
import com.upc.modular.questionbank.entity.QuestionsBanksList;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.service.ITeachingQuestionBankService;
import com.upc.modular.questionbank.controller.param.GradeSubjectiveRequest;
import com.upc.modular.questionbank.controller.param.PendingReviewReturnVO;
import com.upc.modular.questionbank.controller.param.PendingReviewSearchParam;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankWithCreatorReturnParam;
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
@RequestMapping("/teaching-question-bank")
@Api(tags = "题库")
public class TeachingQuestionBankController {
    @Autowired
    private ITeachingQuestionBankService teachingQuestionBankService;

    @Autowired
    private TeacherMapper teacherMapper;

    @ApiOperation("新增题库")
    @PostMapping("/inserQuestionBank")
    public R<Long> inserQuestionBank(@RequestBody TeachingQuestionBank teachingQuestionBank){
        Long result = teachingQuestionBankService.inserQuestionBank(teachingQuestionBank);
        return R.ok(result);
    }

    @ApiOperation("删除题库")
    @PostMapping("deleteQuestionBank")
    public R deleteQuestionBank(@RequestBody IdParam idParam){
        teachingQuestionBankService.deleteQuestionBankByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation("更新题库信息")
    @PostMapping("updateQuestionBank")
    public R updateQuestionBank(@RequestBody TeachingQuestionBank teachingQuestionbank){
        teachingQuestionBankService.updateQuestionBank(teachingQuestionbank);
        return R.commonReturn(200, "修改成功", "");
    }
@ApiOperation("根据题库ID查询题目信息")
@GetMapping("/questions/{bankId}")
public R<List<QuestionsBanksListVO>> getQuestionsByBankId(@ApiParam("题库ID") @PathVariable Long bankId) {
    List<QuestionsBanksListVO> questions = teachingQuestionBankService.getQuestionsWithTypeNameByBankId(bankId);
    return R.ok(questions);
}


    @ApiOperation("根据id查询单个题库信息")
    @PostMapping("selectQuestionBank")
    public R<TeachingQuestionBankWithCreatorReturnParam> selectQuestionBank(@RequestBody TeachingQuestionBank teachingQuestionbank){
        TeachingQuestionBankWithCreatorReturnParam result = teachingQuestionBankService.getQuestionBankWithCreator(teachingQuestionbank.getId());
        return R.ok(result);
    }


    @ApiOperation("分页查询所有题库信息")
    @PostMapping("selectQuestionBankPage")
    public R<PageBaseReturnParam<TeachingQuestionBankPageReturnParam>> selectQuestionBankPage(@RequestBody TeachingQuestionBankPageSearchParam param){
        Page<TeachingQuestionBankPageReturnParam> page = teachingQuestionBankService.selectQuestionBankPage(param);
        PageBaseReturnParam<TeachingQuestionBankPageReturnParam> p = PageBaseReturnParam.ok(page);
        return R.page(p);
    }

    @ApiOperation("点击教材题库后的页面")
    @PostMapping("/listWithStatus")
    public R<List<QuestionBankWithStatusVO>> getQuestionBanksWithStatus(@RequestBody QuestionBankWithStatusSearchParam param) {
        Long userId = UserUtils.get().getId();
        Long teacherId = teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, userId)
        ).getId();
        param.setTeacherId(teacherId);
        List<QuestionBankWithStatusVO> result = teachingQuestionBankService.getQuestionBanksWithStatusForTextbook(param);
        return R.ok(result);
    }

    @ApiOperation("分页查询-查看答题情况")
    @PostMapping("/gradingSituationPage")
    public R<PageBaseReturnParam<GradingSituationReturnVO>> getGradingSituationPage(@RequestBody GradingSituationSearchParam param) {
        Page<GradingSituationReturnVO> pageResult = teachingQuestionBankService.getGradingSituationPage(param);
        PageBaseReturnParam<GradingSituationReturnVO> p = PageBaseReturnParam.ok(pageResult);
        return R.page(p);
    }

    @ApiOperation("查看单个学生的答卷详情")
    @GetMapping("/studentAnswerDetails/{recordId}")
    public R<List<StudentAnswerDetailVO>> getStudentAnswerDetails(@ApiParam(value = "答卷记录ID", required = true) @PathVariable Long recordId) {
        List<StudentAnswerDetailVO> result = teachingQuestionBankService.getStudentAnswerDetails(recordId);
        return R.ok(result);
    }

    @PostMapping("/gradeSubjectiveQuestion")
    @ApiOperation("教师对单道问答题进行评分")
    public R gradeSubjectiveQuestion(@RequestBody GradeSubjectiveRequest request) {
        teachingQuestionBankService.gradeSubjectiveQuestion(request);
        return R.commonReturn(200, "评分成功", null);
    }

    @ApiOperation("点击教材题库-点击批阅-题库批阅-分页查询待批改题目列表")
    @PostMapping("/getPendingReviewPage")
    public R<PageBaseReturnParam<PendingReviewReturnVO>> getPendingReviewPage(@RequestBody PendingReviewSearchParam param) {
        Page<PendingReviewReturnVO> pageResult = teachingQuestionBankService.selectPendingReviewPage(param);
        PageBaseReturnParam<PendingReviewReturnVO> p = PageBaseReturnParam.ok(pageResult);
        return R.page(p);
    }

    @ApiOperation("获取题库作答次数和学生答题次数-APP")
    @PostMapping("/getBankExerAttempAndStudentNum")
    public R<TeachingQuestionBankGetBankExerAttempAndStudentNumReturnParam> getBankExerAttempAndStudentNum(@RequestBody TeachingQuestionBankGetBankExerAttempAndStudentNumSearchParam param){
        TeachingQuestionBankGetBankExerAttempAndStudentNumReturnParam result = teachingQuestionBankService.getBankExerAttempAndStudentNum(param);
        return R.ok(result);
    }
}
