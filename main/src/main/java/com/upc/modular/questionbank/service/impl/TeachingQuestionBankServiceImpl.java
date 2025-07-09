package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.mapper.TeachingQuestionBankMapper;
import com.upc.modular.questionbank.service.ITeachingQuestionBankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Service
public class TeachingQuestionBankServiceImpl extends ServiceImpl<TeachingQuestionBankMapper, TeachingQuestionBank> implements ITeachingQuestionBankService {

    @Autowired
    TeachingQuestionBankMapper teachingQuestionBankMapper;
    @Autowired
    TextbookMapper textbookMapper;
    @Autowired
    TextbookCatalogMapper textbookCatalogMapper;

    @Override
    public Void deleteQuestionBankByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 批量查询
        List<TeachingQuestionBank> found = teachingQuestionBankMapper.selectBatchIds(idList);
        // 如果数量不一致，则说明有遗漏
        if (found.size() != idList.size()) {
            // 找出那些不存在的 ID
            List<Long> foundIds = found.stream()
                    .map(TeachingQuestionBank::getId)
                    .collect(Collectors.toList());
            List<Long> missing = idList.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "未找到对应的题库 ID：" + missing
            );
        }
        this.removeByIds(idList);

        return null;
    }

    @Override
    public Page<TeachingQuestionBank> selectQuestionPage(TeachingQuestionBankPageSearchParam param) {
        Page<TeachingQuestionBank> page = new Page<>(param.getCurrent(), param.getSize());
        return teachingQuestionBankMapper.selectQuestionBank(page, param);
    }

    @Override
    public void inserQuestionBank(TeachingQuestionBank param) {
        Long textbookId = param.getTextbookId();
        Long textbookCatalogId = param.getTextbookCatalogId();

        // 教材ID和教材目录ID是外键，需要判断是否存在
        LambdaQueryWrapper<Textbook> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Textbook::getId,textbookId);
        boolean isTextbookExists = textbookMapper.exists(queryWrapper1);
        if (!isTextbookExists) {
            throw new RuntimeException("ID为 " + textbookId + " 的教材不存在！");
        }

        LambdaQueryWrapper<TextbookCatalog> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(TextbookCatalog::getId,textbookCatalogId);
        boolean isTextbookCatalogExists = textbookCatalogMapper.exists(queryWrapper2);
        if (!isTextbookCatalogExists) {
            throw new RuntimeException("ID为 " + textbookCatalogId + " 的教材目录不存在！");
        }

        this.save(param);
    }

    @Override
    public void updateQuestionBank(TeachingQuestionBank teachingQuestionbank) {
        // 1. 校验要更新的记录本身是否存在
        Long questionBankId = teachingQuestionbank.getId();
        if (questionBankId == null) {
            throw new RuntimeException("更新失败，未提供题库ID！");
        }
        // 使用 getById 查询，比 exists 更优，因为如果存在，后续可能需要用到旧数据
        TeachingQuestionBank oldQuestionBank = this.getById(questionBankId);
        if (oldQuestionBank == null) {
            throw new RuntimeException("ID为 " + questionBankId + " 的题库不存在，无法更新！");
        }

        // 2. 校验外键（教材ID）是否存在
        Long textbookId = teachingQuestionbank.getTextbookId();
        if(ObjectUtils.isNotEmpty(textbookId) && ObjectUtils.isNotNull(textbookId)){
            if (!textbookId.equals(oldQuestionBank.getTextbookId())) {
                boolean isTextbookExists = textbookMapper.exists(
                        new LambdaQueryWrapper<Textbook>().eq(Textbook::getId, textbookId)
                );
                if (!isTextbookExists) {
                    throw new RuntimeException("ID为 " + textbookId + " 的教材不存在！");
                }
            }
        }

        // 3. 校验外键（教材目录ID）是否存在
        Long textbookCatalogId = teachingQuestionbank.getTextbookCatalogId();
        if (ObjectUtils.isNotEmpty(textbookCatalogId) && ObjectUtils.isNotNull(textbookCatalogId)) {
            if (!textbookCatalogId.equals(oldQuestionBank.getTextbookCatalogId())) {
                boolean isTextbookCatalogExists = textbookCatalogMapper.exists(
                        new LambdaQueryWrapper<TextbookCatalog>().eq(TextbookCatalog::getId, textbookCatalogId)
                );
                if (!isTextbookCatalogExists) {
                    throw new RuntimeException("ID为 " + textbookCatalogId + " 的教材目录不存在！");
                }
            }
        }

        // 4. 所有校验通过，执行更新操作
        // updateById 会根据 teachingQuestionbank 对象的ID去更新其他非空字段
        this.updateById(teachingQuestionbank);
    }
}
