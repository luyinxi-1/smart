package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.SmartPaperGenerationParam;
import com.upc.modular.questionbank.controller.param.SmartPaperQuestionVO;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionPageSearchReturnVO;
import com.upc.modular.questionbank.controller.param.QuestionCountByTypeReturnParam;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
public interface ITeachingQuestionService extends IService<TeachingQuestion> {

    Void deleteCourseByIds(IdParam idParam);

    Page<TeachingQuestionPageSearchReturnVO> selectQuestionPage(TeachingQuestionPageSearchParam teachingQuestion);
    TeachingQuestion selectQuestionById(Long id);

    /**
     * 保存题目并自动填充教材和章节名称
     * @param teachingQuestion 题目对象
     */
    void saveQuestionWithTextbookInfo(TeachingQuestion teachingQuestion);

    /**
     * 智能组卷
     * @param param 组卷参数
     * @return 题目列表
     */
    List<SmartPaperQuestionVO> smartPaperGeneration(SmartPaperGenerationParam param);
    
    /**
     * 根据教材ID和章节ID统计各题型题目数量
     * @param textbookId 教材ID
     * @param chapterId 章节ID
     * @return 各题型题目数量列表
     */
    List<QuestionCountByTypeReturnParam> countQuestionsByType(Long textbookId, Long chapterId);

    void batchImportQuestions(MultipartFile file, Long textbookId, Long chapterId);
}