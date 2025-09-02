package com.upc.modular.datastatistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.datastatistics.entity.TeacherStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;



/**
 * 教师统计Mapper接口
 */
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

}