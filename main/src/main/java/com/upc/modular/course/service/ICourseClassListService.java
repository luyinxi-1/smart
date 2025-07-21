package com.upc.modular.course.service;

import com.upc.modular.course.controller.param.ClassInfoReturnParam;
import com.upc.modular.course.controller.param.GetMyCourseReturnParam;
import com.upc.modular.course.entity.CourseClassList;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.course.entity.CourseTextbookList;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ICourseClassListService extends IService<CourseClassList> {

    void associateClasses(Long courseId, List<Long> classIdList);

    List<ClassInfoReturnParam> getClassesByCourse(Long courseId);

    List<GetMyCourseReturnParam> getMyCourse();
}
