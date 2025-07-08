package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
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
}
