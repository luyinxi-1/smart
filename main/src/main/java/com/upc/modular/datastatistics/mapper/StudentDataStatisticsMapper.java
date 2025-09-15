package com.upc.modular.datastatistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.datastatistics.entity.StudentStatisticsData;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.entity.Textbook;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentDataStatisticsMapper  extends BaseMapper<StudentStatisticsData>{
    @Select("select count(DISTINCT textbook_id) from learning_log where creator = #{currentUserId}")
    Long countTextbookByUserId(Long currentUserId);

    @Select("select count(DISTINCT textbook_id) from user_favorites where creator = #{currentUserId}")
    Long countFavoritebookByUserId(Long currentUserId);

    @Select("SELECT COUNT(DISTINCT topic_id) FROM discussion_topic_reply WHERE creator = #{currentUserId}")
    Long countTeachingActivitiesByUserId(Long currentUserId);

    @Select("SELECT COUNT(topic_id) FROM discussion_topic_reply WHERE creator = #{currentUserId}")
    Long countCommunicationByUserId(Long currentUserId);

    @Select("SELECT COUNT(DISTINCT topic_id) FROM discussion_topic_reply WHERE creator = #{currentUserId} AND add_datetime BETWEEN #{startTime} AND #{endTime}")
    Long countCommunicationByUserIdAndTime(Long currentUserId, String startTime, String endTime);

    @Select("SELECT COUNT(*) FROM learning_notes WHERE creator = #{currentUserId}")
    Long countNotesByUserId(Long currentUserId);
    
    @Select("SELECT COUNT(*) FROM learning_notes WHERE creator = #{currentUserId} AND add_datetime BETWEEN #{startTime} AND #{endTime}")
    Long countNotesByUserIdAndTime(Long currentUserId, String startTime, String endTime);

    @Select("SELECT COUNT(DISTINCT name) FROM teaching_question_bank WHERE operator = #{currentUserId}")
    Long countQuestionsByUserId(Long currentUserId);

    @Select("SELECT COUNT(DISTINCT name) FROM teaching_question_bank WHERE operator = #{currentUserId} AND add_datetime BETWEEN #{startTime} AND #{endTime}")
    Long countQuestionsByUserIdAndTime(Long currentUserId, String startTime, String endTime);

    @Select("SELECT * FROM learning_log WHERE creator = #{userId} ORDER BY add_datetime ASC")
    List<LearningLog> findAddDatetime(
            @Param("userId") Long currentUserId,
            @Param("dataType") int dataType);

    @Select("SELECT * FROM learning_log WHERE creator = #{userId} AND EXTRACT(YEAR FROM add_datetime) = #{year} ORDER BY add_datetime ASC")
    List<LearningLog> findAddDatetimeByYear(
            @Param("userId") Long currentUserId,
            @Param("dataType") int dataType,
            @Param("year") Integer year);

    @Select("SELECT DISTINCT textbook_id,catalogue_id FROM learning_log WHERE creator = #{currentUserId} AND data_type = 1")
    List<Map<String, Object>> findReadCatalogsByUserId(Long currentUserId);

    @Select("SELECT * FROM learning_log WHERE creator = #{currentUserId} AND add_datetime BETWEEN #{startTime} AND #{endTime}")
    List<Map<String, Object>> findReadCatalogsByUserIdAndTime(Long currentUserId,String startTime,String endTime);
    @Select("SELECT * FROM textbook where id = #{textbookId}")
    Textbook getTextbookById(Long textbookId);
    @Select("SELECT COUNT(DISTINCT textbook_id) FROM learning_log WHERE creator = #{userId} AND EXTRACT(YEAR FROM add_datetime) = #{year}")
    Long countTextbookByUserIdAndYear(Long userId, int currentYear);

    @Select("SELECT * FROM learning_log WHERE creator = #{userId} AND add_datetime BETWEEN #{startTime} AND #{endTime}")
    List<LearningLog> findAddDatetimeByTime(Long userId, String startTime, String endTime, int type);
    @MapKey("date")
    List<Map<String, Object>> groupReadingTimeByDay(Long userId, String startTime, String endTime);
    @MapKey("date")
    List<Map<String, Object>> groupNotesByDay(Long userId, String startTime, String endTime);
    @MapKey("date")
    List<Map<String, Object>> groupQuestionsByDay(Long userId, String startTime, String endTime);

    @Select("SELECT COUNT(DISTINCT textbook_id) FROM learning_log WHERE creator = #{userId} AND add_datetime BETWEEN #{startTime} AND #{endTime}")
    Long countStudentTextbookReadByTime(Long userId, String startTime, String endTime);


}
