package com.upc.modular.datastatistics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentDataStatisticsMapper {
    @Select("select count(DISTINCT textbook_id) from learning_log where user_id = #{currentUserId}")
    Long countTextbookByUserId(Long currentUserId);

    @Select("select count(DISTINCT textbook_id) from user_favorites where user_id = #{currentUserId}")
    Long countFavoritebookByUserId(Long currentUserId);

    @Select("SELECT COUNT(DISTINCT topic_id) FROM discussion_topic_reply WHERE creator = #{currentUserId}")
    Long countTeachingActivitiesByUserId(Long currentUserId);

    @Select("SELECT COUNT(topic_id) FROM discussion_topic_reply WHERE creator = #{currentUserId}")
    Long countCommunicationByUserId(Long currentUserId);

    @Select("SELECT COUNT(*) FROM learning_notes WHERE creator = #{currentUserId}")
    Long countNotesByUserId(Long currentUserId);

    @Select("SELECT COUNT(DISTINCT name) FROM teaching_question_bank WHERE operator = #{currentUserId}")
    Long countQuestionsByUserId(Long currentUserId);
}
