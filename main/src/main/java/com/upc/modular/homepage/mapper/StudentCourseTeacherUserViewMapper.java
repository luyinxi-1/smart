package com.upc.modular.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.homepage.entity.StudentCourseTeacherUserView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 学生课程教师用户视图 Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-10-31
 */
@Mapper
public interface StudentCourseTeacherUserViewMapper extends BaseMapper<StudentCourseTeacherUserView> {

    /**
     * 根据教师用户ID获取课程班级的学生用户ID列表
     * @param teacherUserId 教师用户ID
     * @return 学生用户ID列表
     */
    @Select("SELECT DISTINCT student_user_id FROM student_course_teacher_user_view WHERE teacher_user_id = #{teacherUserId}")
    List<Long> getStudentUserIdsByTeacherUserId(@Param("teacherUserId") Long teacherUserId);
}