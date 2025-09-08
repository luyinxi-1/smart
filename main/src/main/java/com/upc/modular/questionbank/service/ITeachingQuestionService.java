package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
public interface ITeachingQuestionService extends IService<TeachingQuestion> {

    Void deleteCourseByIds(IdParam idParam);

    Page<TeachingQuestion> selectQuestionPage(TeachingQuestionPageSearchParam teachingQuestion);
    TeachingQuestion selectQuestionById(Long id);
}
