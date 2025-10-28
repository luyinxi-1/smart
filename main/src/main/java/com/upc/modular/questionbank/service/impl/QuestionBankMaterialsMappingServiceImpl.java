package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.UserUtils;
import com.upc.modular.questionbank.controller.param.QuestionBankMaterialsPageParam;
import com.upc.modular.questionbank.entity.QuestionBankMaterialsMapping;
import com.upc.modular.questionbank.mapper.QuestionBankMaterialsMappingMapper;
import com.upc.modular.questionbank.service.IQuestionBankMaterialsMappingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 题库素材关联服务实现类
 * </p>
 *
 * @author cyy
 * @since 2025-10-27
 */
@Service
public class QuestionBankMaterialsMappingServiceImpl extends ServiceImpl<QuestionBankMaterialsMappingMapper, QuestionBankMaterialsMapping> 
        implements IQuestionBankMaterialsMappingService {

    @Override
    public List<QuestionBankMaterialsMapping> getMaterialsByQuestionBankId(Long questionBankId) {
        return baseMapper.selectMaterialsByQuestionBankId(questionBankId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddMaterials(Long questionBankId, List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return true;
        }

        // 获取当前用户ID
        Long currentUserId = UserUtils.get() != null ? UserUtils.get().getId() : null;
        LocalDateTime now = LocalDateTime.now();
        
        List<QuestionBankMaterialsMapping> mappings = new ArrayList<>();
        for (int i = 0; i < materialIds.size(); i++) {
            QuestionBankMaterialsMapping mapping = new QuestionBankMaterialsMapping();
            mapping.setQuestionBankId(questionBankId);
            mapping.setMaterialId(materialIds.get(i));
            mapping.setSequence(i + 1);
            mapping.setCreator(currentUserId);      // 手动设置创建者
            mapping.setAddDatetime(now);            // 手动设置创建时间
            mapping.setOperator(currentUserId);     // 手动设置操作者（新增也是一种操作）
            mapping.setOperationDatetime(now);      // 手动设置操作时间
            mappings.add(mapping);
        }

        return baseMapper.batchInsert(mappings) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRemoveMaterials(Long questionBankId, List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return true;
        }

        LambdaQueryWrapper<QuestionBankMaterialsMapping> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBankMaterialsMapping::getQuestionBankId, questionBankId)
                .in(QuestionBankMaterialsMapping::getMaterialId, materialIds);

        return this.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMaterials(Long questionBankId, List<Long> materialIds) {
        // 先删除该题库的所有素材关联
        baseMapper.deleteByQuestionBankId(questionBankId);

        // 如果新的素材列表为空，直接返回
        if (materialIds == null || materialIds.isEmpty()) {
            return true;
        }

        // 添加新的关联
        return batchAddMaterials(questionBankId, materialIds);
    }
    
    @Override
    public Page<QuestionBankMaterialsMapping> selectMaterialsPageList(QuestionBankMaterialsPageParam param) {
        // 创建分页对象
        Page<QuestionBankMaterialsMapping> page = new Page<>(param.getCurrent(), param.getSize());
        // 调用Mapper层的分页查询方法
        return baseMapper.selectMaterialsPageList(page, param);
    }
}

