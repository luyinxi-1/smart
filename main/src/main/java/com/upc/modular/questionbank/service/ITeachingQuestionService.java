package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.SmartPaperGenerationParam;
import com.upc.modular.questionbank.controller.param.SmartPaperQuestionVO;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

    /**
     * 智能组卷
     * @param param 组卷参数
     * @return 题目列表
     */
    List<SmartPaperQuestionVO> smartPaperGeneration(SmartPaperGenerationParam param);
}
