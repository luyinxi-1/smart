package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.mapper.LearningLogMapper;
import com.upc.modular.textbook.param.RecentStudyReturnParam;
import com.upc.modular.textbook.service.ILearningLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Autowired
    private LearningLogMapper learningLogMapper;

    @Override
    public Boolean insert(LearningLog learningLog) {
        if (ObjectUtils.isEmpty(learningLog) || ObjectUtils.isEmpty(learningLog.getTextbookId()) || ObjectUtils.isEmpty(learningLog.getCatalogueId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        if (ObjectUtils.isEmpty(learningLog.getCreator())) {
            learningLog.setCreator(UserUtils.get().getId());
        }
        return this.save(learningLog);
    }

    @Override
    public List<RecentStudyReturnParam> recentStudy(Integer limit) {
        if (ObjectUtils.isEmpty(UserUtils.get()) || ObjectUtils.isEmpty(UserUtils.get().getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }
        Long userId = UserUtils.get().getId();
        // Call the mapper method to get the recent study list
        List<RecentStudyReturnParam> recentStudies = learningLogMapper.recentStudy(userId, limit);
        return recentStudies;
    }
}
