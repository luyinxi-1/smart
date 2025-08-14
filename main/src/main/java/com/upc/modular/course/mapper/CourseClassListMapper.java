package com.upc.modular.course.mapper;

import com.upc.modular.course.controller.param.GetMyCourseReturnParam;
import com.upc.modular.course.controller.param.GetMyCourseSearchParam;
import com.upc.modular.course.entity.CourseClassList;
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
public interface CourseClassListMapper extends BaseMapper<CourseClassList> {
    List<GetMyCourseReturnParam> getMyCourseStudent(@Param("id") Long id, @Param("param") GetMyCourseSearchParam param);

    List<GetMyCourseReturnParam> getMyCourseTeacher(@Param("id") Long id, @Param("param") GetMyCourseSearchParam param);

    List<GetMyCourseReturnParam> getMyCourseAdmin(@Param("id") Long id, @Param("param") GetMyCourseSearchParam param);

}
