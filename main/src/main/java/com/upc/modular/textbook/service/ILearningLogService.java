package com.upc.modular.textbook.service;

import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.LearningLog;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
