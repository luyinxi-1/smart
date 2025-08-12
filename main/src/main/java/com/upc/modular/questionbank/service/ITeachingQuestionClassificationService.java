package com.upc.modular.questionbank.service;

import com.upc.modular.questionbank.controller.param.TeachingQuestionClassificationSearchParam;
import com.upc.modular.questionbank.controller.param.TopLevelTeachingQuestionClassificationSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestionClassification;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author la
 * @since 2025-08-12
 */
public interface ITeachingQuestionClassificationService extends IService<TeachingQuestionClassification> {

    void insertTeachingQuestionClassification(TeachingQuestionClassification param);

    void removeTeachingQuestionClassification(List<Long> idList);

    boolean updateTeachingQuestionClassification(TeachingQuestionClassification param);

    List<TeachingQuestionClassification> selectTeachingQuestionClassificationParentIdList(Integer classificationGrade);

    List<TeachingQuestionClassification> selectTeachingQuestionClassificationDownList(Long id);

    List<TeachingQuestionClassification> selectTeachingQuestionClassificationList(TeachingQuestionClassificationSearchParam param);

    List<TeachingQuestionClassification> buildDictTree(List<TeachingQuestionClassification> list);

    boolean updateTeachingQuestionClassificationSortName(Long id, Integer param);

    List<TeachingQuestionClassification> selectTopLevelTeachingQuestionClassification();
}
