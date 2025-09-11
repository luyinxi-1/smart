package com.upc.modular.questionbank.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.controller.param.QuestionsBanksListPageSearchParam;
import com.upc.modular.questionbank.controller.param.QuestionsBanksListVO;
import com.upc.modular.questionbank.entity.QuestionsBanksList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.questionbank.entity.TeachingQuestion;
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
public interface QuestionsBanksListMapper extends BaseMapper<QuestionsBanksList> {

    Page<QuestionsBanksList> selectQuestionPageList(Page<QuestionsBanksList> page, @Param("param") QuestionsBanksListPageSearchParam param);
   List<QuestionsBanksListVO> selectQuestionsWithTypeNameByBankId(@Param("bankId") Long bankId);
}
