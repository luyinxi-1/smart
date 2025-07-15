package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.upc.modular.textbook.mapper.LearningAnnotationsAndLabelsMapper;
import com.upc.modular.textbook.service.ILearningAnnotationsAndLabelsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author la
 * @since 2025-07-15
 */
@Service
public class LearningAnnotationsAndLabelsServiceImpl extends ServiceImpl<LearningAnnotationsAndLabelsMapper, LearningAnnotationsAndLabels> implements ILearningAnnotationsAndLabelsService {

    @Autowired
    private LearningAnnotationsAndLabelsMapper learningAnnotationsAndLabelsMapper;

    @Override
    public Boolean batchDetele(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam) || ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        return this.removeBatchByIds(idParam.getIdList());
    }

    @Override
    public Boolean saveOrUpdateLabels(LearningAnnotationsAndLabels param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getCatalogId()) || ObjectUtils.isEmpty(param.getTextbookId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        MyLambdaQueryWrapper<LearningAnnotationsAndLabels> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LearningAnnotationsAndLabels::getCatalogId, param.getCatalogId())
                .eq(LearningAnnotationsAndLabels::getTextbookId, param.getTextbookId())
                .eq(LearningAnnotationsAndLabels::getCreator, UserUtils.get().getId());
        List<LearningAnnotationsAndLabels> learningAnnotationsAndLabels = learningAnnotationsAndLabelsMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtils.isEmpty(learningAnnotationsAndLabels)) {
            return this.save(param);
        }
        LearningAnnotationsAndLabels result = new LearningAnnotationsAndLabels();
        BeanUtils.copyProperties(learningAnnotationsAndLabels.get(0), result);
        result.setContent(param.getContent());
        return this.updateById(result);
    }
}
