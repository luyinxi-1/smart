package com.upc.modular.questionbank.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchReturnVO;
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

    Page<TeachingQuestionPageSearchReturnVO> selectQuestion(
            Page<TeachingQuestionPageSearchReturnVO> page,
            @Param("param") TeachingQuestionPageSearchParam param,
            @Param("userId") Long userId,
            @Param("isAdmin") boolean isAdmin
    );
    TeachingQuestion selectQuestionById(@Param("id") Long id);

    /**
     * 根据教材ID、章节ID、题型、难度查询题目列表（用于智能组卷）
     * @param textbookId 教材ID
     * @param chapterId 章节ID
     * @param type 题型
     * @param difficulty 难度
     * @return 题目列表
     */
    @Select("SELECT id, type, content, difficulty FROM teaching_question " +
            "WHERE textbook_id = #{textbookId} " +
            "AND chapter_id = #{chapterId} " +
            "AND type = #{type} " +
            "AND difficulty = #{difficulty} " +
            "AND status = 1 " +
            "ORDER BY RAND()")
    java.util.List<TeachingQuestion> selectQuestionsByCondition(
            @Param("textbookId") Long textbookId,
            @Param("chapterId") Long chapterId,
            @Param("type") Integer type,
            @Param("difficulty") Integer difficulty
    );
}


