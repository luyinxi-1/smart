package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.*;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.baomidou.mybatisplus.extension.service.IService;

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

    Page<TeachingQuestionBankPageReturnParam> selectQuestionBankPage(TeachingQuestionBankPageSearchParam param);

    Long inserQuestionBank(TeachingQuestionBank teachingQuestionbank);

    void updateQuestionBank(TeachingQuestionBank teachingQuestionbank);

    /**
     * 批量更新题库信息
     *
     * @param teachingQuestionBanks 待更新的题库实体列表
     */
    //void updateQuestionBankBatch(List<TeachingQuestionBank> teachingQuestionBanks);

    /**
     * 批量更新题库信息
     *
     * @param teachingQuestionBanks 待更新的题库列表
     * @return 成功更新的题库ID列表
     */
    List<Long> updateQuestionBankBatch(List<TeachingQuestionBank> teachingQuestionBanks);

    List<QuestionBankWithStatusVO> getQuestionBanksWithStatusForTextbook(QuestionBankWithStatusSearchParam param);

    /**
     * 获取题库关联的素材列表
     *
     * @param questionBankId 题库ID
     * @return 素材列表
     */
    List<QuestionBankMaterialVO> getQuestionBankMaterials(Long questionBankId);

    Page<GradingSituationReturnVO> getGradingSituationPage(GradingSituationSearchParam param);

    List<StudentAnswerDetailVO> getStudentAnswerDetails(Long recordId);

    List<PendingReviewQuestionVO> getPendingReviewByQuestion(PendingReviewSearchParam param);

    void gradeSubjectiveQuestion(GradeSubjectiveRequest request);

    TeachingQuestionBankWithCreatorReturnParam getQuestionBankWithCreator(Long id);

    List<QuestionsBanksListVO> getQuestionsWithTypeNameByBankId(Long bankId);
    TeachingQuestionBankGetBankExerAttempAndStudentNumReturnParam getBankExerAttempAndStudentNum(TeachingQuestionBankGetBankExerAttempAndStudentNumSearchParam param);
}