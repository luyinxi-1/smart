package com.upc.modular.questionbank.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.controller.param.*;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Mapper
public interface TeachingQuestionBankMapper extends BaseMapper<TeachingQuestionBank> {

    Page<TeachingQuestionBankPageMidReturnParam> selectQuestionBank(Page<TeachingQuestionBankPageMidReturnParam> page, @Param("param") TeachingQuestionBankPageSearchParam param);

    List<QuestionBankWithStatusVO> selectQuestionBanksWithPendingStatus(
            @Param("textbookId") Long textbookId,
            @Param("teacherId") Long currentTeacherId
    );

    Page<GradingSituationReturnVO> selectGradingSituationPage(Page<GradingSituationReturnVO> page, @Param("param") GradingSituationSearchParam param);

    List<StudentAnswerDetailVO> selectStudentAnswerDetailsByRecordId(@Param("recordId") Long recordId);

    Page<PendingReviewReturnVO> selectPendingReviewPage(Page<PendingReviewReturnVO> page, @Param("param") PendingReviewSearchParam param);
}
