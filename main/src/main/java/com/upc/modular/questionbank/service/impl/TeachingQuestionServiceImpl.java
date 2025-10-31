package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.questionbank.controller.param.SmartPaperGenerationParam;
import com.upc.modular.questionbank.controller.param.SmartPaperQuestionVO;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchReturnVO;
import com.upc.modular.questionbank.controller.param.QuestionCountByTypeReturnParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.mapper.TeachingQuestionMapper;
import com.upc.modular.questionbank.service.ITeachingQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
/*    public Page<TeachingQuestion> selectQuestionPage(TeachingQuestionPageSearchParam param) {
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
    }*/
    @Override
    public Page<TeachingQuestionPageSearchReturnVO> selectQuestionPage(TeachingQuestionPageSearchParam param) {
        // 获取当前登录用户的 ID
        Long userId = UserUtils.get().getId();
        // 2. 根据 userId 从数据库实时查询用户信息
        SysTbuser currentUser = sysUserMapper.selectById(userId);
        // 如果用户不存在，可以进行异常处理
        if (currentUser == null) {
            throw new RuntimeException("当前登录用户不存在！");
        }
        // 3. 从查询到的用户对象中获取 userType
        Integer userType = currentUser.getUserType();

        // 根据 user_type 判断是否为管理员
        boolean isAdmin = (userType != null && userType == 0);
        // 创建分页对象
        Page<TeachingQuestionPageSearchReturnVO> page = new Page<>(param.getCurrent(), param.getSize());

        // 调用 Mapper 方法，并传入 isAdmin 标志
        Page<TeachingQuestionPageSearchReturnVO> resultPage = teachingQuestionMapper.selectQuestion(page, param, userId, isAdmin);

        return resultPage;
    }

    @Override
    public TeachingQuestion selectQuestionById(Long id) {
        return teachingQuestionMapper.selectQuestionById(id); //
    }

    @Override
    public List<SmartPaperQuestionVO> smartPaperGeneration(SmartPaperGenerationParam param) {
        // 参数校验
        if (param.getTextbookId() == null || param.getChapterId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID和章节ID不能为空");
        }
        if (param.getDifficulty() == null || param.getDifficulty() < 1 || param.getDifficulty() > 3) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "难易程度必须为1-3之间");
        }
        if (param.getQuestionTypeCount() == null || param.getQuestionTypeCount().isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "题型数量不能为空");
        }

        List<SmartPaperQuestionVO> result = new ArrayList<>();

        // 遍历每个题型
        for (Map.Entry<Integer, Integer> entry : param.getQuestionTypeCount().entrySet()) {
            Integer questionType = entry.getKey();
            Integer totalCount = entry.getValue();

            if (totalCount == null || totalCount <= 0) {
                continue; // 跳过无效的数量
            }

            // 计算选择的难易程度的题目数量（向上取整）
            int selectedDifficultyCount = (int) Math.ceil(totalCount * 0.5);

            // 计算其余难易程度平分的数量
            int remainingCount = totalCount - selectedDifficultyCount;

            // 获取所有难度级别（1-简单，2-中等，3-困难）
            List<Integer> otherDifficulties = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                if (i != param.getDifficulty()) {
                    otherDifficulties.add(i);
                }
            }

            // 计算每个其他难度应分配的题目数量
            int eachOtherCount = otherDifficulties.size() > 0 ? remainingCount / otherDifficulties.size() : 0;
            int remainder = otherDifficulties.size() > 0 ? remainingCount % otherDifficulties.size() : 0;

            // 从选择的难易程度中抽取题目
            List<TeachingQuestion> selectedQuestions = teachingQuestionMapper.selectQuestionsByCondition(
                    param.getTextbookId(),
                    param.getChapterId(),
                    questionType,
                    param.getDifficulty()
            );

            if (selectedQuestions.size() < selectedDifficultyCount) {
                throw new BusinessException(
                        BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        String.format("题型%d的难度%d题目数量不足，需要%d道，实际只有%d道",
                                questionType, param.getDifficulty(), selectedDifficultyCount, selectedQuestions.size())
                );
            }

            // 添加选择的难度的题目
            result.addAll(convertToVO(selectedQuestions.subList(0, selectedDifficultyCount)));

            // 从其他难度中抽取题目
            for (int i = 0; i < otherDifficulties.size(); i++) {
                Integer otherDifficulty = otherDifficulties.get(i);
                // 如果有余数，前面几个难度多分配一道题
                int countForThisDifficulty = eachOtherCount + (i < remainder ? 1 : 0);

                if (countForThisDifficulty > 0) {
                    List<TeachingQuestion> otherQuestions = teachingQuestionMapper.selectQuestionsByCondition(
                            param.getTextbookId(),
                            param.getChapterId(),
                            questionType,
                            otherDifficulty
                    );

                    if (otherQuestions.size() < countForThisDifficulty) {
                        throw new BusinessException(
                                BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                                String.format("题型%d的难度%d题目数量不足，需要%d道，实际只有%d道",
                                        questionType, otherDifficulty, countForThisDifficulty, otherQuestions.size())
                        );
                    }

                    result.addAll(convertToVO(otherQuestions.subList(0, countForThisDifficulty)));
                }
            }
        }

        return result;
    }

    /**
     * 将TeachingQuestion转换为SmartPaperQuestionVO
     */
    private List<SmartPaperQuestionVO> convertToVO(List<TeachingQuestion> questions) {
        return questions.stream().map(question -> {
            SmartPaperQuestionVO vo = new SmartPaperQuestionVO();
            vo.setId(question.getId());
            vo.setType(question.getType());
            vo.setContent(question.getContent());
            vo.setDifficulty(question.getDifficulty());
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<QuestionCountByTypeReturnParam> countQuestionsByType(Long textbookId, Long chapterId) {
        // 查询数据库获取各题型数量
        List<QuestionCountByTypeReturnParam> result = teachingQuestionMapper.countQuestionsByType(textbookId, chapterId);
        
        return result;
    }
}