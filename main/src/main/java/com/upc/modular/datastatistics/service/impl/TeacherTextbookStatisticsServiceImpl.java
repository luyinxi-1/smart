package com.upc.modular.datastatistics.service.impl;

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
} 