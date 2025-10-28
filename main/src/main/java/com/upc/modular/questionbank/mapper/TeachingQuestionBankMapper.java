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

    Page<TeachingQuestionBankPageMidReturnParam> selectQuestionBank(
            Page<TeachingQuestionBankPageMidReturnParam> page,
            @Param("param") TeachingQuestionBankPageSearchParam param,
            @Param("userId") Long userId,
            @Param("userRole") String userRole
    );

    List<QuestionBankWithStatusVO> selectQuestionBanksWithPendingStatus(
//            @Param("param") QuestionBankWithStatusSearchParam param
            @Param("textbookId") Long textbookId,
            @Param("textbookCatalogId") Long textbookCatalogId,
            @Param("teachingQuestionBankName") String teachingQuestionBankName
    );

    Page<GradingSituationReturnVO> selectGradingSituationPage(Page<GradingSituationReturnVO> page, @Param("param") GradingSituationSearchParam param);

    List<StudentAnswerDetailVO> selectStudentAnswerDetailsByRecordId(@Param("recordId") Long recordId);

    List<PendingReviewRawDataVO> selectPendingReviewRawDataForBank(@Param("param") PendingReviewSearchParam param);


}