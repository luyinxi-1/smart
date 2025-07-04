package com.upc.modular.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.course.controller.param.CourseDataExportSearchParam;
import com.upc.modular.course.controller.param.CoursePageReturnParam;
import com.upc.modular.course.controller.param.CoursePageSearchParam;
import com.upc.modular.course.entity.Course;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ICourseService extends IService<Course> {

    Void deleteCourseByIds(IdParam idParam);

    Page<CoursePageReturnParam> getPage(CoursePageSearchParam param);

    void exportCourseData(HttpServletResponse response, IdParam param);
}
