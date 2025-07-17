package com.upc.modular.course.mapper;

import com.upc.modular.course.controller.param.CourseTextbookListReturnParam;
import com.upc.modular.course.entity.CourseTextbookList;
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
public interface CourseTextbookListMapper extends BaseMapper<CourseTextbookList> {

    List<CourseTextbookListReturnParam> selectCourseTextbookList(@Param("courseId") Long courseId);
}
