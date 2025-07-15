package com.upc.modular.textbook.service;

import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
/**
 * <p>
 *  服务类
 * </p>
 *
 * @author la
 * @since 2025-07-15
 */
public interface ILearningAnnotationsAndLabelsService extends IService<LearningAnnotationsAndLabels> {
    Boolean batchDetele(IdParam idParam);

    Boolean saveOrUpdateLabels(LearningAnnotationsAndLabels param);

    List<LearningAnnotationsAndLabels> selectLabels(Long textbookId);
}
