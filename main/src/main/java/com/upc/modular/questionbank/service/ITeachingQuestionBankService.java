package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.*;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.questionbank.controller.param.PendingReviewReturnVO;
import com.upc.modular.questionbank.controller.param.PendingReviewSearchParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
public interface ITeachingQuestionBankService extends IService<TeachingQuestionBank> {

    Void deleteQuestionBankByIds(IdParam idParam);

    Page<TeachingQuestionBankPageReturnParam> selectQuestionPage(TeachingQuestionBankPageSearchParam param);

    Long inserQuestionBank(TeachingQuestionBank teachingQuestionbank);

    void updateQuestionBank(TeachingQuestionBank teachingQuestionbank);

    List<QuestionBankWithStatusVO> getQuestionBanksWithStatusForTextbook(QuestionBankWithStatusSearchParam param);

    Page<GradingSituationReturnVO> getGradingSituationPage(GradingSituationSearchParam param);

    List<StudentAnswerDetailVO> getStudentAnswerDetails(Long recordId);

    Page<PendingReviewReturnVO> selectPendingReviewPage(PendingReviewSearchParam param);

    void gradeSubjectiveQuestion(GradeSubjectiveRequest request);
}
