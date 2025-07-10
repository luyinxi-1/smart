package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.QuestionsBanksListPageSearchParam;
import com.upc.modular.questionbank.entity.QuestionsBanksList;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.mapper.QuestionsBanksListMapper;
import com.upc.modular.questionbank.mapper.TeachingQuestionBankMapper;
import com.upc.modular.questionbank.mapper.TeachingQuestionMapper;
import com.upc.modular.questionbank.service.IQuestionsBanksListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
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
public class QuestionsBanksListServiceImpl extends ServiceImpl<QuestionsBanksListMapper, QuestionsBanksList> implements IQuestionsBanksListService {
    @Autowired
    TeachingQuestionMapper teachingQuestionMapper;
    @Autowired
    TeachingQuestionBankMapper teachingQuestionBankMapper;
    @Autowired
    QuestionsBanksListMapper questionsBanksListMapper;

    @Override
    public Void inserQuestionBankList(QuestionsBanksList param) {
        Long questionId = param.getQuestionId();
        Long bankId = param.getBankId();

        // 题目ID和题库ID是外键，需要判断是否存在
        LambdaQueryWrapper<TeachingQuestion> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(TeachingQuestion::getId,questionId);
        boolean isTeachingQuestionExists = teachingQuestionMapper.exists(queryWrapper1);
        if (!isTeachingQuestionExists) {
            throw new RuntimeException("ID为 " + questionId + " 的题目不存在！");
        }

        LambdaQueryWrapper<TeachingQuestionBank> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(TeachingQuestionBank::getId,bankId);
        boolean isTeachingQuestionBankExists = teachingQuestionBankMapper.exists(queryWrapper2);
        if (!isTeachingQuestionBankExists) {
            throw new RuntimeException("ID为 " + bankId + " 的题库不存在！");
        }

        this.save(param);
        return null;
    }

    @Override
    public void deleteQuestionsBanksListByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 批量查询数据库中id对应idList的
        List<QuestionsBanksList> found = questionsBanksListMapper.selectBatchIds(idList);
        // 如果数量不一致，则说明有遗漏
        if (found.size() != idList.size()) {
            // 找出那些不存在的 ID
            List<Long> foundIds = found.stream()
                    .map(QuestionsBanksList::getId)
                    .collect(Collectors.toList());
            List<Long> missing = idList.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "未找到对应的题库题目关联 ID：" + missing
            );
        }
        this.removeByIds(idList);

    }

    @Override
    public void updateQuestionsBanksList(QuestionsBanksList param) {
        // 1. 校验要更新的记录本身是否存在
        Long questionBankListId = param.getId();
        if (questionBankListId == null) {
            throw new RuntimeException("更新失败，未提供题目题库关联ID！");
        }
        // 使用 getById 查询，比 exists 更优，因为如果存在，后续可能需要用到旧数据
        QuestionsBanksList oldQuestionBankList = this.getById(questionBankListId);
        if (oldQuestionBankList == null) {
            throw new RuntimeException("ID为 " + questionBankListId + " 的题目题库关联不存在，无法更新！");
        }

        // 2. 校验外键（题目ID）是否存在
        Long questionId = param.getQuestionId();
        if(ObjectUtils.isNotEmpty(questionId) && ObjectUtils.isNotNull(questionId)){
            if (!questionId.equals(oldQuestionBankList.getQuestionId())) {
                boolean isQuestionBankListExists = teachingQuestionMapper.exists(
                        new LambdaQueryWrapper<TeachingQuestion>().eq(TeachingQuestion::getId, questionId)
                );
                if (!isQuestionBankListExists) {
                    throw new RuntimeException("ID为 " + questionId + " 的题目不存在！");
                }
            }
        }

        // 3. 校验外键（教材目录ID）是否存在
        Long bankId = param.getBankId();
        if (ObjectUtils.isNotEmpty(bankId) && ObjectUtils.isNotNull(bankId)) {
            if (!bankId.equals(oldQuestionBankList.getBankId())) {
                boolean isQuestionBankListExists = teachingQuestionBankMapper.exists(
                        new LambdaQueryWrapper<TeachingQuestionBank>().eq(TeachingQuestionBank::getId, bankId)
                );
                if (!isQuestionBankListExists) {
                    throw new RuntimeException("ID为 " + bankId + " 的题库不存在！");
                }
            }
        }

        // 4. 所有校验通过，执行更新操作
        // updateById 会根据 teachingQuestionbank 对象的ID去更新其他非空字段
        this.updateById(param);
    }

    @Override
    public Page<QuestionsBanksList> selectQuestionPageList(QuestionsBanksListPageSearchParam param) {
        Page<QuestionsBanksList> page = new Page<>(param.getCurrent(), param.getSize());
        return questionsBanksListMapper.selectQuestionPageList(page, param);
    }
}
