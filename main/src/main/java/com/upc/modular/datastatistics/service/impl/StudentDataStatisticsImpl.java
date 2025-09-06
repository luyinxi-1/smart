package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.UserUtils;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.datastatistics.controller.param.StudentReadingTimeByMonthReturnParam;
import com.upc.modular.datastatistics.controller.param.StudentTextbookCompletionReturnParam;
import com.upc.modular.datastatistics.entity.StudentStatisticsData;
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
public class StudentDataStatisticsImpl extends ServiceImpl<StudentDataStatisticsMapper,StudentStatisticsData> implements IStudentDataStatistics {
    // 完成度阈值（完成阅读）
    private static final long COMPLETION_THRESHOLD = 70L;
    @Autowired
    private StudentDataStatisticsMapper studentDataStatisticsMapper;

    // 注入教材目录服务
    @Autowired
    private ITextbookCatalogService textbookCatalogService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 统计学生阅读的教材数量
     */
    @Override
    public Long countStudentTextbookReading() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countTextbookByUserId(currentUserId);

    }
    /**
     * 统计学生书架的书籍数量
     */
    @Override
    public Long countStudentFavoritebook() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countFavoritebookByUserId(currentUserId);
    }
    /**
     * 统计学生参与的教学活动数量
     */
    @Override
    public Long countStudentTeachingActivities() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countTeachingActivitiesByUserId(currentUserId);

    }
    /**
     * 统计学生交流反馈数量
     */
    @Override
    public Long countStudentCommunicationFeedback() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countCommunicationByUserId(currentUserId);
    }
    /**
     * 统计学生笔记数量
     */
    @Override
    public Long countStudentnotes() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countNotesByUserId(currentUserId);
    }
    /**
     * 统计学生答题数量
     */
    @Override
    public Long countStudentQuestions() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countQuestionsByUserId(currentUserId);
    }
    /**
     * 统计学生教材总阅读时间
     */
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
    /**
     * 按月统计学生教材阅读时间
     * @param year 年份
     */
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
    /**
     * 统计学生教材完成度
     */
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
     * 统计学生完成阅读的教材数量
     */
    @Override
    public Long countStudentTextbookRead() {
        List<StudentTextbookCompletionReturnParam> completionList = countStudentTextbookCompetion();

        //统计完成度为指定值的教材数量
        long completedTextbooks = completionList.stream()
                .filter(param -> param.getCompletion() != null && param.getCompletion() >= COMPLETION_THRESHOLD)
                .count();
        return completedTextbooks;
    }

    /**
     * 统计学生今年教材阅读时长
     */
    @Override
    public List<StudentStatisticsData> countStudentCurrentYearTextbookReadingTime() {
        Long userId = UserUtils.get().getId();
        int currentYear = LocalDateTime.now().getYear();
        // 容忍范围
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;
        // 获取时间列表
        List<LearningLog> records;
        records = studentDataStatisticsMapper.findAddDatetimeByYear(userId, 0, currentYear);
        if (records == null || records.isEmpty() || records.size() < 2) {
            return Collections.emptyList();
        }
        //计算本年阅读时间
        long totalReadingTime = 0L;
        for (int i = 0; i < records.size() - 1; i++) {
            LocalDateTime currentAddDatetime = records.get(i).getAddDatetime();
            LocalDateTime nextAddDatetime = records.get(i + 1).getAddDatetime();
            if (currentAddDatetime == null || nextAddDatetime == null) {
                continue;
            }
            Duration duration = Duration.between(currentAddDatetime, nextAddDatetime);
            long seconds = duration.getSeconds();
            if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                totalReadingTime += 1;
            }
        }

        //将统计信息存入到student_statistic_data中
        Student student = studentMapper.selectOne(new QueryWrapper<Student>().lambda()
                .eq(Student::getUserId, userId));
        StudentStatisticsData statisticsData = new StudentStatisticsData();
        statisticsData.setId(student.getId());
        statisticsData.setReadingTime(totalReadingTime);
        statisticsData.setUserId(userId);

        SysTbuser sysTbuser = sysUserMapper.selectOne(new QueryWrapper<SysTbuser>().lambda()
                .eq(SysTbuser::getId, userId));
        statisticsData.setPicUrl(sysTbuser.getUserPicture());

        statisticsData.setUpdateTime(LocalDateTime.now());
        this.saveOrUpdate(statisticsData);

        // 查询所有学生统计数据并按reading_time降序排序
        List<StudentStatisticsData> sortedStatistics = this.list(new QueryWrapper<StudentStatisticsData>().lambda()
                .orderByDesc(StudentStatisticsData::getReadingTime));

        return sortedStatistics;
    }
    /**
     * 统计学生今年教材阅读数量
     */
    @Override
    public List<StudentStatisticsData> countStudentCurrentTextbookRead() {
        Long userId = UserUtils.get().getId();
        int currentYear = LocalDateTime.now().getYear();

        // 统计今年阅读的教材数量
        Long currentYearTextbookCount = studentDataStatisticsMapper.countTextbookByUserIdAndYear(userId, currentYear);
        if (currentYearTextbookCount == null) {
            currentYearTextbookCount = 0L;
        }

        // 获取学生信息
        Student student = studentMapper.selectOne(new QueryWrapper<Student>().lambda()
                .eq(Student::getUserId, userId));

        // 获取用户信息
        SysTbuser sysTbuser = sysUserMapper.selectOne(new QueryWrapper<SysTbuser>().lambda()
                .eq(SysTbuser::getId, userId));

        // 构造统计数据实体
        StudentStatisticsData statisticsData = new StudentStatisticsData();
        if (student != null) {
            statisticsData.setId(student.getId());
        }
        statisticsData.setUserId(userId);
        statisticsData.setReadingBook(currentYearTextbookCount);
        if (sysTbuser != null) {
            statisticsData.setPicUrl(sysTbuser.getUserPicture());
        }
        statisticsData.setUpdateTime(LocalDateTime.now());

        // 保存或更新统计数据
        this.saveOrUpdate(statisticsData);

        // 查询所有学生统计数据并按reading_book降序排序
        List<StudentStatisticsData> sortedStatistics = this.list(new QueryWrapper<StudentStatisticsData>().lambda()
                .orderByDesc(StudentStatisticsData::getReadingBook));

        return sortedStatistics;

    }

    @Override
    public Long countStudentTextbookReadingTimeByTime(String startTime, String endTime) {
        Long userId = UserUtils.get().getId();
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;
        List<LearningLog> records = studentDataStatisticsMapper.findAddDatetimeByTime(userId, startTime, endTime,0);
        if(records == null || records.size() < 2){
            return 0L;
        }
        long totalReadingTime = 0L;
        for (int i = 0; i < records.size() - 1; i++) {
            LocalDateTime currentAddDatetime = records.get(i).getAddDatetime();
            LocalDateTime nextAddDatetime = records.get(i + 1).getAddDatetime();
            if (currentAddDatetime == null || nextAddDatetime == null) {
                continue;
            }
            Duration duration = Duration.between(currentAddDatetime, nextAddDatetime);
            long seconds = duration.getSeconds();
            if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                totalReadingTime += 1;
            }
        }
        return totalReadingTime;
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
