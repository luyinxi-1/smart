package com.upc.modular.questionbank.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Mapper
public interface TeachingQuestionMapper extends BaseMapper<TeachingQuestion> {

//    Page<TeachingQuestion> selectCourse(Page<TeachingQuestion> page, TeachingQuestionPageSearchParam param);

    Page<TeachingQuestion> selectQuestion(
            Page<TeachingQuestion> page,
            @Param("param") TeachingQuestionPageSearchParam param,
            @Param("userId") Long userId
    );
    TeachingQuestion selectQuestionById(@Param("id") Long id);
}


