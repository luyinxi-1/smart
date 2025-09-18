package com.upc.modular.datastatistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.datastatistics.entity.TeacherStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherStatisticsMapper extends BaseMapper<TeacherStatistics> {

    /**
     * 统计教师授课班级数量
     */
    Integer countTeacherClasses(@Param("teacherId") Long teacherId);

    /**
     * 统计教师授课学生数量
     */
    Integer countTeacherStudents(@Param("teacherId") Long teacherId);

    /**
     * 统计教师教材数量
     */
    Integer countTeacherTextbooks(@Param("teacherId") Long teacherId);

    /**
     * 统计教师授课课程数量
     */
    Integer countTeacherCourses(@Param("teacherId") Long teacherId);

    /**
     * 获取班级学生ID列表
     */
    List<Long> getClassStudentIds(@Param("classId") Long classId);

    /**
     * 获取班级学生详细信息
     */
    List<Map<String, Object>> getClassStudentDetails(@Param("classId") Long classId);

    /**
     * 统计班级学生在指定时间范围内的阅读时长
     */
    Long getClassTotalReadingTime(@Param("classId") Long classId,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime);

    /**
     * 统计班级学生在指定时间范围内的阅读数量
     */
    Long getClassTotalReadingNum(@Param("classId") Long classId,
                                 @Param("startTime") String startTime,
                                 @Param("endTime") String endTime);

    /**
     * 统计班级学生在指定时间范围内的笔记数量
     */
    Long getClassTotalNotesNum(@Param("classId") Long classId,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime);

    /**
     * 统计班级学生在指定时间范围内的答题数量
     */
    Long getClassTotalQuestionBankNum(@Param("classId") Long classId,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime);

    /**
     * 统计班级学生在指定时间范围内的交流反馈数量
     */
    Long getClassTotalCommunicationFeedbackNum(@Param("classId") Long classId,
                                               @Param("startTime") String startTime,
                                               @Param("endTime") String endTime);
}
