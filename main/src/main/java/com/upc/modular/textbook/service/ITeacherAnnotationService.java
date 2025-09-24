package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.entity.TeacherAnnotation;
import com.upc.modular.textbook.param.TeacherAnnotationPageSearchParam;
import com.upc.modular.textbook.param.TeacherAnnotationReturnParam;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mjh
 * @since 2025-09-02
 */
public interface ITeacherAnnotationService extends IService<TeacherAnnotation> {

    Long insertTeacherAnnotation(TeacherAnnotation param);

    TeacherAnnotationReturnParam getTeacherAnnotation(Long id);

    Page<TeacherAnnotationReturnParam> getTeacherAnnotationPage(TeacherAnnotationPageSearchParam param);
}
