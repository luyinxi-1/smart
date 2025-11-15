package com.upc.modular.textbook.service;

import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.LearningLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.RecentStudyReturnParam;
import com.upc.modular.textbook.param.UuidParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
public interface ILearningLogService extends IService<LearningLog> {

    Boolean insert(LearningLog learningLog);

    List<RecentStudyReturnParam> recentStudy(Integer limit);
    
    Boolean batchDeleteByUuid(UuidParam uuidParam);



    /**
     * 【批量】获取指定用户在多本书籍下所有未同步的学习笔记
     */
    List<LearningLog> getNewLogsBatch(Long userId, List<Long> textbookIds);

    /**
     * 【批量】根据ID列表确认指定用户和书籍的学习笔记同步状态
     */
    boolean confirmLogsSyncBatch(Long userId, List<Long> textbookIds, List<Long> syncedIds);
}