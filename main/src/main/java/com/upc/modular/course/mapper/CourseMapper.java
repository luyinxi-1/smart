package com.upc.modular.course.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.course.controller.param.CoursePageReturnParam;
import com.upc.modular.course.controller.param.CoursePageSearchParam;
import com.upc.modular.course.controller.param.GetMyCourseReturnParam;
import com.upc.modular.course.entity.Course;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    Page<CoursePageReturnParam> selectCourse(
            Page<CoursePageReturnParam> page,
            @Param("param") CoursePageSearchParam param
    );
    
    Page<CoursePageReturnParam> selectCourseByTeacher(
            Page<CoursePageReturnParam> page,
            @Param("param") CoursePageSearchParam param,
            @Param("teacherId") Long teacherId
    );
}