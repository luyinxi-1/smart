package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningAnnotationsAndLabels;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.mapper.LearningAnnotationsAndLabelsMapper;
import com.upc.modular.textbook.param.UuidParam;
import com.upc.modular.textbook.service.ILearningAnnotationsAndLabelsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public Boolean batchDeleteByUuid(UuidParam uuidParam) {
        // 1. 参数校验
        if (ObjectUtils.isEmpty(uuidParam) || ObjectUtils.isEmpty(uuidParam.getUuidList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参UUID列表不能为空");
        }

        // 2. 构建删除条件 (QueryWrapper)
        // 使用 LambdaQueryWrapper 可以防止硬编码字段名，更加安全
        LambdaQueryWrapper<LearningAnnotationsAndLabels> wrapper = new LambdaQueryWrapper<>();
        // 关键：构建 WHERE uuid IN ('uuid1', 'uuid2', ...) 的条件
        wrapper.in(LearningAnnotationsAndLabels::getClientUuid, uuidParam.getUuidList());

        // 3. 执行删除操作
        // this.remove(wrapper) 方法会根据构造的条件执行 DELETE FROM table WHERE ...
        return this.remove(wrapper);
    }


    @Override
    public Boolean saveOrUpdateLabels(LearningAnnotationsAndLabels param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getCatalogId()) || ObjectUtils.isEmpty(param.getTextbookId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        // 默认设置为未同步状态
        param.setSyncStatus(0);
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
        result.setPositionInfo(param.getPositionInfo());
        return this.updateById(result);
    }

    @Override
    public List<LearningAnnotationsAndLabels> selectLabels(Long textbookId) {
        if (ObjectUtils.isEmpty(textbookId) || ObjectUtils.isEmpty(UserUtils.get().getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参不能为空");
        }
        MyLambdaQueryWrapper<LearningAnnotationsAndLabels> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(LearningAnnotationsAndLabels::getTextbookId, textbookId);
        lambdaQueryWrapper.eq(LearningAnnotationsAndLabels::getCreator, UserUtils.get().getId());
        return learningAnnotationsAndLabelsMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public List<LearningAnnotationsAndLabels> selectLabelsByCatalogId(Long textbookId, Long catalogId) {
        if (ObjectUtils.isEmpty(textbookId) || ObjectUtils.isEmpty(catalogId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID和目录ID不能为空");
        }
        return this.list(new LambdaQueryWrapper<LearningAnnotationsAndLabels>()
                .eq(LearningAnnotationsAndLabels::getTextbookId, textbookId)
                .eq(LearningAnnotationsAndLabels::getCatalogId, catalogId));
    }
    @Override
    public List<LearningAnnotationsAndLabels> getNewAnnotationsBatch(Long userId, List<Long> textbookIds) {
        if (textbookIds == null || textbookIds.isEmpty()) {
            return new ArrayList<>();
        }
        return this.lambdaQuery()
                .eq(LearningAnnotationsAndLabels::getSyncStatus, 0)
                .eq(LearningAnnotationsAndLabels::getCreator, userId)
                .in(LearningAnnotationsAndLabels::getTextbookId, textbookIds) // 核心：使用 IN 查询
                .list();
    }

    @Override
    @Transactional
    public boolean confirmAnnotationsSyncBatch(Long userId, List<Long> textbookIds, List<Long> syncedIds) {
        if (syncedIds == null || syncedIds.isEmpty()) {
            return true;
        }
        return this.lambdaUpdate()
                .eq(LearningAnnotationsAndLabels::getSyncStatus, 0)
                .eq(LearningAnnotationsAndLabels::getCreator, userId)
                .in(LearningAnnotationsAndLabels::getTextbookId, textbookIds) // 确保只在这些书的范围内
                .in(LearningAnnotationsAndLabels::getId, syncedIds) // 核心：只更新这些ID
                .set(LearningAnnotationsAndLabels::getSyncStatus, 1)
                .update();
    }

}