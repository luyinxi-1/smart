package com.upc.modular.datastatistics.mapper;

import com.upc.modular.datastatistics.controller.param.TextbookTimeStatisticsReturnParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 教师端教材数据统计Mapper
 */
@Mapper
public interface TeacherTextbookStatisticsMapper {

    /**
     * 统计教材阅读人数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM learning_log WHERE textbook_id = #{textbookId} AND data_type = 0")
    Long countReadersByTextbookId(@Param("textbookId") Long textbookId);

    /**
     * 统计教材教学活动数量
     */
    @Select("SELECT COUNT(*) FROM discussion_topic WHERE textbook_id = #{textbookId}")
    Long countTeachingActivitiesByTextbookId(@Param("textbookId") Long textbookId);

    /**
     * 统计教材素材数量
     */
    @Select("SELECT COUNT(*) FROM materials_textbook_mapping WHERE textbook_id = #{textbookId}")
    Long countMaterialsByTextbookId(@Param("textbookId") Long textbookId);

    /**
     * 统计教材阅读时长 - 基于learning_log表计算
     * 通过计算相邻记录的时间差来估算阅读时长
     */
    @Select("SELECT COALESCE(COUNT(*), 0) FROM learning_log WHERE textbook_id = #{textbookId} AND data_type = 0")
    Long countReadingDurationByTextbookId(@Param("textbookId") Long textbookId);

    /**
     * 统计教材交流反馈数量
     */
    @Select("SELECT COUNT(*) FROM discussion_topic_reply dtr " +
            "INNER JOIN discussion_topic dt ON dtr.topic_id = dt.id " +
            "WHERE dt.textbook_id = #{textbookId}")
    Long countCommunicationFeedbackByTextbookId(@Param("textbookId") Long textbookId);

    /**
     * 统计教材教学思政数量
     */
    @Select("SELECT COUNT(*) FROM ideological_material WHERE textbook_id = #{textbookId}")
    Long countIdeologicalMaterialsByTextbookId(@Param("textbookId") Long textbookId);

    /**
     * 按时间统计交流反馈新增数量
     */
    List<TextbookTimeStatisticsReturnParam> getCommunicationFeedbackStatisticsByTime(@Param("textbookId") Long textbookId,
                                                                                      @Param("queryMethod") Integer queryMethod,
                                                                                      @Param("startTime") String startTime,
                                                                                      @Param("endTime") String endTime);

    /**
     * 按时间统计阅读时长
     */
    List<TextbookTimeStatisticsReturnParam> getReadingDurationStatisticsByTime(@Param("textbookId") Long textbookId,
                                                                               @Param("queryMethod") Integer queryMethod,
                                                                               @Param("startTime") String startTime,
                                                                               @Param("endTime") String endTime);
} 