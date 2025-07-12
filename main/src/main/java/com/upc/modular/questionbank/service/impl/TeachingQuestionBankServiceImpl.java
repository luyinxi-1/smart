package com.upc.modular.questionbank.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageMidReturnParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageReturnParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.mapper.TeachingQuestionBankMapper;
import com.upc.modular.questionbank.service.ITeachingQuestionBankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @Autowired
    TeacherMapper teacherMapper;

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
    public Page<TeachingQuestionBankPageReturnParam> selectQuestionPage(TeachingQuestionBankPageSearchParam param) {
        // --- 第一阶段：数据库查询 ---
        // 1. 创建分页对象，注意泛型是中间结果类型
        Page<TeachingQuestionBankPageMidReturnParam> page = new Page<>(param.getCurrent(), param.getSize());

        // 2. 调用Mapper获取包含 creatorId 和 questionCount 的分页数据
        Page<TeachingQuestionBankPageMidReturnParam> midResultPage = teachingQuestionBankMapper.selectQuestionBank(page, param);

        // --- 第二阶段：Java内存中数据处理和转换 ---
        List<TeachingQuestionBankPageMidReturnParam> midRecords = midResultPage.getRecords();

        // 如果查询结果为空，直接返回一个空的最终分页对象，保留分页信息
        if (CollectionUtils.isEmpty(midRecords)) {
            return new Page<>(midResultPage.getCurrent(), midResultPage.getSize(), 0);
        }

        // 3. 提取所有不重复的创建者ID (creator)
        Set<Long> creatorIds = midRecords.stream()
                .map(TeachingQuestionBankPageMidReturnParam::getCreator)
                .filter(id -> id != null) // 过滤掉可能为null的id
                .collect(Collectors.toSet());

        // 4. 一次性查询所有相关的教师姓名
        Map<Long, String> creatorIdToNameMap;
        if (!creatorIds.isEmpty()) {
            // 假设 Teacher 实体中，用户ID是 userId，姓名是 name
            List<Teacher> teachers = teacherMapper.selectList(
                    new LambdaQueryWrapper<Teacher>()
                            .in(Teacher::getUserId, creatorIds)
            );
            // 将查询结果转为 Map<ID, Name> 以便快速查找
            creatorIdToNameMap = teachers.stream()
                    .collect(Collectors.toMap(Teacher::getUserId, Teacher::getName, (oldValue, newValue) -> oldValue));
        } else {
            creatorIdToNameMap = Collections.emptyMap();
        }

        // 5. 将中间结果(MidReturnParam)转换为最终结果(ReturnParam)
        List<TeachingQuestionBankPageReturnParam> finalRecords = midRecords.stream().map(midParam -> {
            TeachingQuestionBankPageReturnParam finalParam = new TeachingQuestionBankPageReturnParam();
            // 复制所有同名属性
            BeanUtils.copyProperties(midParam, finalParam);
            // 单独处理 creator 字段，从ID转换为Name
            String creatorName = creatorIdToNameMap.getOrDefault(midParam.getCreator(), "未知用户"); // 如果找不到，给个默认值
            finalParam.setCreator(creatorName);
            return finalParam;
        }).collect(Collectors.toList());

        // 6. 构建并返回最终的分页对象
        Page<TeachingQuestionBankPageReturnParam> finalPage = new Page<>(
                midResultPage.getCurrent(),
                midResultPage.getSize(),
                midResultPage.getTotal()
        );
        finalPage.setRecords(finalRecords);

        return finalPage;
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
