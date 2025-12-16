package com.upc.modular.datastatistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.auth.entity.SysDictData;
import com.upc.modular.course.entity.CourseClassList;
import com.upc.modular.datastatistics.entity.StudentStatisticsData;
import com.upc.modular.student.entity.Student;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.entity.Textbook;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface StudentDataStatisticsMapper extends BaseMapper<StudentStatisticsData> {
    @Select("select count(DISTINCT textbook_id) from learning_log where creator = #{currentUserId}")
    Long countTextbookByUserId(Long currentUserId);
    
    // 批量查询多个用户的教材阅读总数
    @Select("<script>" +
            "SELECT COUNT(DISTINCT textbook_id) FROM learning_log WHERE creator IN " +
            "<foreach item='item' collection='userIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    Long countTextbookByUserIds(@Param("userIds") List<Long> userIds);

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
            @Param("userId") Long currentUserId);

    // 新增的：根据 studentId 统计各教材阅读时长
    List<Map<String, Object>> getTextbookReadingDurationByStudentId(@Param("studentId") Long studentId);
    
    @Select("SELECT COUNT(*) as total_reading_time " +
            "FROM ( " +
            "  SELECT " +
            "    EXTRACT(EPOCH FROM age(LEAD(add_datetime, 1) OVER (PARTITION BY creator ORDER BY add_datetime), add_datetime)) as diff_seconds " +
            "  FROM learning_log " +
            "  WHERE creator = #{userId} " +
            ") t " +
            "WHERE diff_seconds BETWEEN 55 AND 65")
    Long getStudentReadingTimeByUserId(@Param("userId") Long userId);
    
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
    
    @Select("SELECT * FROM learning_log WHERE creator = #{userId} AND textbook_id IS NOT NULL AND catalogue_id IS NOT NULL AND add_datetime BETWEEN #{startTime} AND #{endTime} ORDER BY textbook_id, catalogue_id, add_datetime")
    List<LearningLog> findChapterRecordsByTime(@Param("userId") Long userId, @Param("startTime") String startTime, @Param("endTime") String endTime, int type);

    @MapKey("activity_date")
    List<Map<String, Object>> groupReadingTimeByDay(Long userId, String startTime, String endTime);

    @MapKey("activity_date")
    List<Map<String, Object>> groupNotesByDay(Long userId, String startTime, String endTime);

    @MapKey("activity_date")
    List<Map<String, Object>> groupQuestionsByDay(Long userId, String startTime, String endTime);

    @Select("SELECT COUNT(DISTINCT textbook_id) FROM learning_log WHERE creator = #{userId} AND add_datetime BETWEEN #{startTime} AND #{endTime}")
    Long countStudentTextbookReadByTime(Long userId, String startTime, String endTime);


    @Select("SELECT min_catalogue from learning_log where creator = #{userId} and textbook_id = #{textbookId} order by add_datetime desc limit 1")
    Long findLastReadingCatalogueId(Long userId, Long textbookId);

    @Select("SELECT catalogue_id FROM learning_log WHERE creator = #{userId} AND textbook_id = #{textbookId}")
    List<Long> findCatalogueIdList(Long userId, Long textbookId);

    @Select("SELECT * FROM learning_log WHERE creator = #{userId} AND textbook_id = #{textbookId} ORDER BY add_datetime ASC")
    List<LearningLog> findLearningLogsByUserAndTextbook(@Param("userId") Long userId, @Param("textbookId") Long textbookId);

    Long countStudySessionsByTextbook(@Param("userId") Long userId, @Param("textbookId") Long textbookId);

    List<Long> listStudentCourseTextbookIds(Long userId);
    
    /**
     * 获取学生的平均得分率
     * @param studentId 学生ID
     * @return 平均得分率
     */
    @Select("SELECT " +
            "CASE " +
            "    WHEN COALESCE(SUM(qbl_total.total_score), 0) = 0 THEN 0.0 " +
            "    ELSE COALESCE(ROUND((SUM(sfg.final_score) / SUM(qbl_total.total_score)) * 100, 2), 0.0) " +
            "END " +
            "FROM student_final_grade sfg " +
            "LEFT JOIN ( " +
            "    SELECT " +
            "        bank_id, " +
            "        SUM(score) AS total_score " +
            "    FROM questions_banks_list " +
            "    GROUP BY bank_id " +
            ") qbl_total ON sfg.bank_id = qbl_total.bank_id " +
            "WHERE sfg.student_id = #{studentId}")
    Double getStudentScoreRate(@Param("studentId") Long studentId);

    @Select("SELECT * FROM learning_log WHERE textbook_id = #{textbookId} ORDER BY add_datetime ASC")
    List<LearningLog> findLearningLogsByTextbookId(@Param("textbookId") Long textbookId);

    // 添加getStudentQuestionAnsweringStatistics方法的声明
    List<Map<String, Object>> getStudentQuestionAnsweringStatistics(@Param("textbookId") Long textbookId, @Param("studentId") Long studentId);
}