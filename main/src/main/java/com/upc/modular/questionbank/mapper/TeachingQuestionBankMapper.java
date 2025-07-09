package com.upc.modular.questionbank.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    Page<TeachingQuestionBank> selectQuestionBank(Page<TeachingQuestionBank> page,@Param("param") TeachingQuestionBankPageSearchParam param);
}
