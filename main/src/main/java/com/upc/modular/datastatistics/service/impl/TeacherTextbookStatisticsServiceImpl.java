package com.upc.modular.datastatistics.service.impl;

import com.upc.modular.datastatistics.controller.param.ChapterQuestionCorrectRateParam;
import com.upc.modular.datastatistics.controller.param.TextbookDataStatisticsParam;
import com.upc.modular.datastatistics.controller.param.TextbookTimeStatisticsReturnParam;
import com.upc.modular.datastatistics.controller.param.TextbookTimeStatisticsSearchParam;
import com.upc.modular.datastatistics.mapper.TeacherTextbookStatisticsMapper;
import com.upc.modular.datastatistics.service.ITeacherTextbookStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教师端教材数据统计服务实现
 */
@Slf4j
@Service
public class TeacherTextbookStatisticsServiceImpl implements ITeacherTextbookStatisticsService {

    @Autowired
    private TeacherTextbookStatisticsMapper teacherTextbookStatisticsMapper;

    @Override
    public TextbookDataStatisticsParam getTextbookDataStatistics(Long textbookId) {
        log.info("获取教材数据统计，教材ID: {}", textbookId);
        
        TextbookDataStatisticsParam param = new TextbookDataStatisticsParam();
        
        // 统计阅读人数
        Long readerCount = teacherTextbookStatisticsMapper.countReadersByTextbookId(textbookId);
        param.setReaderCount(readerCount);
        
        // 统计教学活动数量
        Long teachingActivityCount = teacherTextbookStatisticsMapper.countTeachingActivitiesByTextbookId(textbookId);
        param.setTeachingActivityCount(teachingActivityCount);
        
        // 统计素材数量
        Long materialCount = teacherTextbookStatisticsMapper.countMaterialsByTextbookId(textbookId);
        param.setMaterialCount(materialCount);
        
        // 统计阅读时长
        Long readingDurationMinutes = teacherTextbookStatisticsMapper.countReadingDurationByTextbookId(textbookId);
        param.setReadingDurationMinutes(readingDurationMinutes);
        
        // 统计交流反馈数量
        Long communicationFeedbackCount = teacherTextbookStatisticsMapper.countCommunicationFeedbackByTextbookId(textbookId);
        param.setCommunicationFeedbackCount(communicationFeedbackCount);
        
        // 统计教学思政数量
        Long ideologicalMaterialCount = teacherTextbookStatisticsMapper.countIdeologicalMaterialsByTextbookId(textbookId);
        param.setIdeologicalMaterialCount(ideologicalMaterialCount);
        
        // 统计答题正确率 - 按照您的思路实现（百分比形式）
        // 步骤1：查出教材对应的所有题库
        // 步骤2：统计每个题库的总分  
        // 步骤3：计算每条记录的正确率并求平均
        Double questionCorrectRate = teacherTextbookStatisticsMapper.getQuestionCorrectRateByTextbookId(textbookId);
        param.setQuestionCorrectRate(questionCorrectRate != null ? questionCorrectRate : 0.0);
        
        log.info("教材数据统计完成，教材ID: {}, 统计结果: {}", textbookId, param);
        return param;
    }

    @Override
    public List<TextbookTimeStatisticsReturnParam> getCommunicationFeedbackStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        log.info("按时间统计交流反馈新增数量，参数: {}", param);
        
        List<TextbookTimeStatisticsReturnParam> result = teacherTextbookStatisticsMapper
                .getCommunicationFeedbackStatisticsByTime(
                        param.getTextbookId(),
                        param.getQueryMethod(),
                        param.getStartTime(),
                        param.getEndTime()
                );
        
        log.info("按时间统计交流反馈新增数量完成，结果数量: {}", result.size());
        return result;
    }

    @Override
    public List<TextbookTimeStatisticsReturnParam> getReadingDurationStatisticsByTime(TextbookTimeStatisticsSearchParam param) {
        log.info("按时间统计阅读时长，参数: {}", param);
        
        List<TextbookTimeStatisticsReturnParam> result = teacherTextbookStatisticsMapper
                .getReadingDurationStatisticsByTime(
                        param.getTextbookId(),
                        param.getQueryMethod(),
                        param.getStartTime(),
                        param.getEndTime()
                );
        
        log.info("按时间统计阅读时长完成，结果数量: {}", result.size());
        return result;
    }

    @Override
    public List<ChapterQuestionCorrectRateParam> getChapterQuestionCorrectRateStatistics(Long textbookId) {
        log.info("获取各章节习题正确率统计，教材ID: {}", textbookId);
        
        List<ChapterQuestionCorrectRateParam> result = teacherTextbookStatisticsMapper
                .getChapterQuestionCorrectRateStatistics(textbookId);
        
        log.info("各章节习题正确率统计完成，教材ID: {}, 结果数量: {}", textbookId, result.size());
        return result;
    }
} 