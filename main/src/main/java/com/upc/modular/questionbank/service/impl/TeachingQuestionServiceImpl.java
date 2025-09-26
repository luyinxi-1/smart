package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.course.controller.param.CoursePageReturnParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.mapper.TeachingQuestionMapper;
import com.upc.modular.questionbank.service.ITeachingQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class TeachingQuestionServiceImpl extends ServiceImpl<TeachingQuestionMapper, TeachingQuestion> implements ITeachingQuestionService {

    @Autowired
    TeachingQuestionMapper teachingQuestionMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Void deleteCourseByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 批量查询
        List<TeachingQuestion> found = teachingQuestionMapper.selectBatchIds(idList);
        // 如果数量不一致，则说明有遗漏
        if (found.size() != idList.size()) {
            // 找出那些不存在的 ID
            List<Long> foundIds = found.stream()
                    .map(TeachingQuestion::getId)
                    .collect(Collectors.toList());
            List<Long> missing = idList.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "未找到对应的题目 ID：" + missing
            );
        }
        this.removeByIds(idList);

        return null;
    }

    @Override
    public Page<TeachingQuestion> selectQuestionPage(TeachingQuestionPageSearchParam param) {
        Long userId = UserUtils.get().getId();
        Page<TeachingQuestion> page = new Page<>(param.getCurrent(), param.getSize());
        Page<TeachingQuestion> resultPage = teachingQuestionMapper.selectQuestion(page, param, userId);

        // 设置是否为当前用户创建的字段
        resultPage.getRecords().forEach(question -> {
            if (question.getCreator() != null) {
                question.setIsCreatedByCurrentUser(question.getCreator().equals(userId));
            } else {
                question.setIsCreatedByCurrentUser(false);
            }
        });

        return resultPage;
    }

    @Override
    public TeachingQuestion selectQuestionById(Long id) {
        return teachingQuestionMapper.selectQuestionById(id); // 👈 使用自定义SQL
    }
}
