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
     * 【新】获取所有未同步的学习日志ID
     * @return ID列表
     */
    List<Long> getNewLogIdsForClient();

    /**
     * 【新】根据ID列表获取学习日志实体
     * @param ids ID列表
     * @return 实体列表
     */
    List<LearningLog> getLogsByIds(List<Long> ids);

    /**
     * 【新】根据ID列表确认同步状态
     * @param ids ID列表
     * @return 是否成功
     */
    boolean confirmLogsSync(List<Long> ids);
}