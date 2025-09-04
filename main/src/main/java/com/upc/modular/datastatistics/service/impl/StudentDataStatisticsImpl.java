package com.upc.modular.datastatistics.service.impl;

import ch.qos.logback.classic.Logger;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.common.utils.UserUtils;
import com.upc.modular.datastatistics.controller.param.StudentReadingTimeByMonthReturnParam;
import com.upc.modular.datastatistics.controller.param.StudentTextbookCompletionReturnParam;
import com.upc.modular.datastatistics.mapper.StudentDataStatisticsMapper;
import com.upc.modular.datastatistics.service.IStudentDataStatistics;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.textbook.entity.LearningLog;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.param.TextbookTree;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentDataStatisticsImpl implements IStudentDataStatistics {
    @Autowired
    private StudentDataStatisticsMapper studentDataStatisticsMapper;

    // 注入教材目录服务
    @Autowired
    private ITextbookCatalogService textbookCatalogService;
    @Override
    public Long countStudentTextbookReading() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countTextbookByUserId(currentUserId);

    }

    @Override
    public Long countStudentFavoritebook() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countFavoritebookByUserId(currentUserId);
    }

    @Override
    public Long countStudentTeachingActivities() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countTeachingActivitiesByUserId(currentUserId);

    }

    @Override
    public Long countStudentCommunicationFeedback() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countCommunicationByUserId(currentUserId);
    }

    @Override
    public Long countStudentnotes() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countNotesByUserId(currentUserId);
    }

    @Override
    public Long countStudentQuestions() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countQuestionsByUserId(currentUserId);
    }

    @Override
    public Long countStudentTextbookReadingTime() {
        Long currentUserId = UserUtils.get().getId();
//        容忍范围
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;
//        获取时间列表
        List<LearningLog> records = studentDataStatisticsMapper.findAddDatetime(currentUserId,0);

        if(records == null || records.size() < 2){
            return 0L;
        }
//        计算总时间
        long totalReadingTime = 0;
        for (int i = 0; i < records.size() - 1; i++) {
            LocalDateTime currentAddDatetime = records.get(i).getAddDatetime();
            LocalDateTime nextAddDatetime = records.get(i + 1).getAddDatetime();
            if(currentAddDatetime == null || nextAddDatetime == null){
                continue;
            }
            Duration duration = Duration.between(currentAddDatetime, nextAddDatetime);
            long seconds = duration.getSeconds();
            if(seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS){
                totalReadingTime += 1;//假设定时数据间隔时间为1秒
            }
        }
        return totalReadingTime;
    }

    @Override
    public List<StudentReadingTimeByMonthReturnParam> countStudentTextbookReadingTimeByMonth(Integer year) {
        Long currentUserId = UserUtils.get().getId();
//        获取12个月的时间
        List<LearningLog> records = studentDataStatisticsMapper.findAddDatetimeByYear(currentUserId,0,year);
//        按月统计时间
        List<StudentReadingTimeByMonthReturnParam> result = new ArrayList<>();
        for(int i = 1; i <= 12; i++){
            result.add(new StudentReadingTimeByMonthReturnParam().setMonth(i).setReadingTime(0L));
        }
        if(records ==  null || records.size() < 2){
            return result;
        }
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;

        for (int i = 0; i < records.size() - 1; i++) {
            LocalDateTime currentAddDatetime = records.get(i).getAddDatetime();
            LocalDateTime nextAddDatetime = records.get(i + 1).getAddDatetime();
            if (currentAddDatetime == null || nextAddDatetime == null) {
                continue;
            }
            if (currentAddDatetime.getMonthValue() != nextAddDatetime.getMonthValue()) {
                continue;
            }
            Duration duration = Duration.between(currentAddDatetime, nextAddDatetime);
            long seconds = duration.getSeconds();

            if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                int month = currentAddDatetime.getMonthValue();
                StudentReadingTimeByMonthReturnParam param = result.get(month - 1);
                param.setReadingTime(param.getReadingTime() + 1);
            }
        }

        return result;
    }

    @Override
    public List<StudentTextbookCompletionReturnParam> countStudentTextbookCompetion() {
        Long currentUserId = UserUtils.get().getId();
        // 获取学生已读的章节信息
        List<Map<String, Object>> readCatalogs = studentDataStatisticsMapper.findReadCatalogsByUserId(currentUserId);

        //如果没有阅读记录，返回空列表
        if (readCatalogs == null || readCatalogs.isEmpty()) {
            return new ArrayList<>();
        }
        //按教材id分组已读章节
        Map<Long, Set<Long>> readCatalogsByBook = readCatalogs.stream()
                .collect(Collectors.groupingBy(
                        map -> (Long) map.get("textbook_id"),
                        Collectors.mapping(map -> (Long) map.get("catalogue_id"), Collectors.toSet())
                        ));

        Set<Long> textbookIds = readCatalogsByBook.keySet();
        List<StudentTextbookCompletionReturnParam> result = new ArrayList<>();
        for (Long textbookId : textbookIds) {
            // 获取教材信息
            Textbook textbook = studentDataStatisticsMapper.getTextbookById(textbookId);
            if (textbook == null) {
                continue;
            }

            // 获取该教材的总章节数
            int totalChapters = countTotalChapters(textbookId);
            if (totalChapters > 0) {
                // 获取该学生已读的章节数
                int readChapters = readCatalogsByBook.getOrDefault(textbookId, Collections.emptySet()).size();

                // 计算完成度百分比
                double completion = (double) readChapters / totalChapters * 100;

                result.add(new StudentTextbookCompletionReturnParam()
                        .setTextbookId(textbookId)
                        .setTextbookName(textbook.getTextbookName())
                        .setCompletion((long) completion));
            } else {
                // 如果教材没有章节，则完成度为0%
                result.add(new StudentTextbookCompletionReturnParam()
                        .setTextbookId(textbookId)
                        .setTextbookName(textbook.getTextbookName())
                        .setCompletion(0L));
            }
        }
        return result;
    }

    /**
     * 计算教材的总章节数
     * @param textbookId 教材ID
     * @return 总章节数
     */
    private int countTotalChapters(Long textbookId) {
        List<TextbookTree> tree = textbookCatalogService.getTextbookCatalogTree(textbookId);
        if(tree == null){
            return 0;
        }
        return countTreeNodes(tree);
    }

    /**
     * 递归计算树节点总数
     * @param nodes 树节点列表
     * @return 节点总数
     */
    private int countTreeNodes(List<TextbookTree> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (TextbookTree node : nodes) {
            count++; // 计算当前节点
            count += countTreeNodes(node.getChildren()); // 递归计算子节点
        }
        return count;
    }

}
