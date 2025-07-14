package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.mapper.LearningLogMapper;
import com.upc.modular.textbook.service.ILearningLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
@Service
public class LearningLogServiceImpl extends ServiceImpl<LearningLogMapper, LearningLog> implements ILearningLogService {

    @Override
    public Boolean insert(LearningLog learningLog) {
        if (ObjectUtils.isEmpty(learningLog) || ObjectUtils.isEmpty(learningLog.getTextbookId()) || ObjectUtils.isEmpty(learningLog.getCatalogueId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        if (ObjectUtils.isEmpty(learningLog.getUserId())) {
            learningLog.setUserId(UserUtils.get().getId());
        }
        return this.save(learningLog);
    }
}
