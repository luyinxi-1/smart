package com.upc.modular.textbook.service;

import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.UuidParam;

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

    /**
     * 根据UUID列表批量删除
     * @param uuidParam 包含UUID列表的参数对象
     * @return 是否删除成功
     */
    Boolean batchDeleteByUuid(UuidParam uuidParam);
    Boolean saveOrUpdateLabels(LearningAnnotationsAndLabels param);

    List<LearningAnnotationsAndLabels> selectLabels(Long textbookId);

    List<LearningAnnotationsAndLabels> selectLabelsByCatalogId(Long textbookId, Long catalogId);
}
