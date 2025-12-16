package com.upc.modular.questionbank.service.impl;

import com.alibaba.excel.EasyExcel;
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
import com.upc.modular.questionbank.entity.QuestionImportDTO;
import com.upc.modular.questionbank.entity.QuestionImportListener;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.mapper.TeachingQuestionMapper;
import com.upc.modular.questionbank.service.ITeachingQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.param.TextbookSpecifiedCatalogSearchParam;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import com.upc.modular.textbook.service.ITextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private ITextbookCatalogService textbookCatalogService;

    @Override
    public void batchImportQuestions(MultipartFile file, Long textbookId, Long chapterId) {
        // 1. 预先查询教材和章节信息 (避免在 Loop 中查库)
        String tbName = null;
        if (textbookId != null) {
            Textbook textbook = textbookService.getById(textbookId);
            if (textbook != null) {
                tbName = textbook.getTextbookName();
            }
        }

        String catName = null;
        if (chapterId != null) {
            TextbookCatalog chapter = textbookCatalogService.getById(chapterId);
            if (chapter != null) {
                catName = chapter.getCatalogName();
            }
        }

        // 2. 启动 EasyExcel 读取
        try {
            EasyExcel.read(file.getInputStream(), QuestionImportDTO.class,
                            // 传入 Service 实例以及预查好的名称信息
                            new QuestionImportListener(this, textbookId, tbName, chapterId, catName))
                    .sheet()
                    .doRead();
        } catch (IOException e) {
            throw new RuntimeException("Excel文件读取失败", e);
        }
    }

    @Override
    public void saveQuestionWithTextbookInfo(TeachingQuestion teachingQuestion) {
        // 如果传入了教材ID，则自动获取教材名称
        if (teachingQuestion.getTextbookId() != null) {
            Textbook textbook = textbookService.getById(teachingQuestion.getTextbookId());
            if (textbook != null) {
                teachingQuestion.setTextbookName(textbook.getTextbookName());
            }
        }

        // 如果传入了章节ID，则自动获取章节名称
        if (teachingQuestion.getChapterId() != null) {
            TextbookCatalog chapter = textbookCatalogService.getById(teachingQuestion.getChapterId());
            if (chapter != null) {
                teachingQuestion.setChapterName(chapter.getCatalogName());
            }
        }

        // 保存题目
        this.save(teachingQuestion);
    }
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

        // 处理章节名称，去除HTML标签
        if (!CollectionUtils.isEmpty(resultPage.getRecords())) {
            for (TeachingQuestionPageSearchReturnVO vo : resultPage.getRecords()) {
                String chapterName = vo.getChapterName();
                if (chapterName != null) {
                    chapterName = com.upc.utils.HtmlUtils.stripHtml(chapterName);
                    vo.setChapterName(chapterName);
                }
            }
        }

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

        // 获取指定章节及其所有子章节的ID列表
        TextbookSpecifiedCatalogSearchParam searchParam = new TextbookSpecifiedCatalogSearchParam();
        searchParam.setTextbookId(param.getTextbookId());
        searchParam.setCatalogId(param.getChapterId());
        List<Long> chapterIds = textbookCatalogService.getTextbookSpecifiedCatalog(searchParam);

        // 初始化结果列表
        List<SmartPaperQuestionVO> result = new ArrayList<>();

        // 遍历每种题型及其需要的数量
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

            // 从选择的难易程度中抽取题目
            List<TeachingQuestion> selectedQuestions = teachingQuestionMapper.selectQuestionsByConditionWithChapters(
                    param.getTextbookId(),
                    chapterIds,
                    questionType,
                    param.getDifficulty()
            );

            // 实际从选择的难度中能获取的题目数量
            int actualSelectedCount = Math.min(selectedQuestions.size(), selectedDifficultyCount);
            // 如果选择的难度题目不足，计算缺口并分配到其他难度
            int deficit = selectedDifficultyCount - actualSelectedCount;

            // 添加选择的难度的可用题目
            if (actualSelectedCount > 0) {
                result.addAll(convertToVO(selectedQuestions.subList(0, actualSelectedCount)));
            }

            // 重新计算其他难度需要的题目数量，加上缺口部分
            int totalOtherCount = remainingCount + deficit;
            
            // 先收集所有其他难度的可用题目
            List<TeachingQuestion> allOtherQuestions = new ArrayList<>();
            for (Integer otherDifficulty : otherDifficulties) {
                List<TeachingQuestion> otherQuestions = teachingQuestionMapper.selectQuestionsByConditionWithChapters(
                        param.getTextbookId(),
                        chapterIds,
                        questionType,
                        otherDifficulty
                );
                allOtherQuestions.addAll(otherQuestions);
            }

            // 检查总的可用题目是否足够
            if (actualSelectedCount + allOtherQuestions.size() < totalCount) {
                throw new BusinessException(
                        BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        String.format("题型%d的题目总数量不足，需要%d道，实际只有%d道（难度%d有%d道，其他难度共%d道）",
                                questionType, totalCount, actualSelectedCount + allOtherQuestions.size(),
                                param.getDifficulty(), actualSelectedCount, allOtherQuestions.size())
                );
            }

            // 从其他难度中取所需数量的题目
            if (totalOtherCount > 0) {
                result.addAll(convertToVO(allOtherQuestions.subList(0, Math.min(totalOtherCount, allOtherQuestions.size()))));
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
        // 获取指定章节及其所有子章节的ID列表
        TextbookSpecifiedCatalogSearchParam param = new TextbookSpecifiedCatalogSearchParam();
        param.setTextbookId(textbookId);
        param.setCatalogId(chapterId);
        List<Long> chapterIds = textbookCatalogService.getTextbookSpecifiedCatalog(param);
        
        // 使用章节ID列表查询数据库获取各题型数量
        List<QuestionCountByTypeReturnParam> result = teachingQuestionMapper.countQuestionsByTypeWithChapters(textbookId, chapterIds);
        
        // 创建一个包含所有题型的映射，确保即使题型没有题目也会显示
        Map<Integer, QuestionCountByTypeReturnParam> typeMap = new HashMap<>();
        
        // 初始化所有可能的题型（1-7）
        for (int i = 1; i <= 7; i++) {
            QuestionCountByTypeReturnParam returnTypeParam = new QuestionCountByTypeReturnParam();
            returnTypeParam.setTypeId(i);
            returnTypeParam.setCount(0L);
            
            // 设置题型名称
            switch (i) {
                case 1:
                    returnTypeParam.setTypeName("单选题");
                    break;
                case 2:
                    returnTypeParam.setTypeName("多选题");
                    break;
                case 3:
                    returnTypeParam.setTypeName("判断题");
                    break;
                case 4:
                    returnTypeParam.setTypeName("填空题");
                    break;
                case 5:
                    returnTypeParam.setTypeName("简答题");
                    break;
                case 6:
                    returnTypeParam.setTypeName("计算题");
                    break;
                case 7:
                    returnTypeParam.setTypeName("论述题");
                    break;
                default:
                    returnTypeParam.setTypeName("未知题型");
                    break;
            }
            
            typeMap.put(i, returnTypeParam);
        }
        
        // 更新实际有题目的题型数量
        for (QuestionCountByTypeReturnParam item : result) {
            typeMap.get(item.getTypeId()).setCount(item.getCount());
        }
        
        // 返回完整的结果列表
        return new ArrayList<>(typeMap.values());
    }
}