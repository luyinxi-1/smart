package com.upc.modular.textbook.service;

import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.entity.LearningLog;
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
    /**
     * 【批量】获取指定用户在多本书籍下所有未同步的学习笔记
     */
    List<LearningAnnotationsAndLabels> getNewAnnotationsBatch(Long userId, List<Long> textbookIds);

    /**
     * 【批量】根据ID列表确认指定用户和书籍的学习笔记同步状态
     */
    boolean confirmAnnotationsSyncBatch(Long userId, List<Long> textbookIds, List<Long> syncedIds);

}