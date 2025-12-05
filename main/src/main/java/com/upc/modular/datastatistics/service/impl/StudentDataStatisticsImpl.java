package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.utils.UserUtils;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.course.entity.Course;
import com.upc.modular.course.entity.CourseClassList;
import com.upc.modular.course.service.ICourseClassListService;
import com.upc.modular.course.service.ICourseService;
import com.upc.modular.course.service.ICourseTextbookListService;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.mapper.GroupMapper;
import com.upc.modular.teacher.mapper.TeacherMapper;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
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

    @Autowired
    private ISysUserService sysUserService;
    
    // 新增注入GroupMapper
    @Autowired
    private GroupMapper groupMapper;
    
    // 新增注入TeacherMapper
    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private ICourseClassListService courseClassListService;

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

    private Long countStudentCommunicationFeedbackByTime(String startTime, String endTime) {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countCommunicationByUserIdAndTime(currentUserId,startTime,endTime);
    }
    /**
     * 统计学生笔记数量
     */
    @Override
    public Long countStudentnotes() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countNotesByUserId(currentUserId);
    }

    private Long countStudentNotesByTime(String startTime, String endTime) {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countNotesByUserIdAndTime(currentUserId,startTime,endTime);
    }
    /**
     * 统计学生答题数量
     */
    @Override
    public Long countStudentQuestions() {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countQuestionsByUserId(currentUserId);
    }

    private Long countStudentQuestionsByTime(String startTime, String endTime) {
        Long currentUserId = UserUtils.get().getId();
        return studentDataStatisticsMapper.countQuestionsByUserIdAndTime(currentUserId,startTime,endTime);
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
        List<LearningLog> records = studentDataStatisticsMapper.findAddDatetime(currentUserId);

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
    public List<StudentTextbookCompletionReturnParam> countStudentTextbookCompetion() {
        return countStudentTextbookCompetion(null, null);
    }

    @Override
    public List<StudentTextbookCompletionReturnParam> countStudentTextbookCompetion(String startTime, String endTime) {
        Long currentUserId = UserUtils.get().getId();
        // 获取学生已读的章节信息
        List<Map<String, Object>> readCatalogs;
        if (startTime == null && endTime == null) {
            readCatalogs = studentDataStatisticsMapper.findReadCatalogsByUserId(currentUserId);
        }else {
            readCatalogs = studentDataStatisticsMapper.findReadCatalogsByUserIdAndTime(currentUserId,startTime,endTime);
        }

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
    public Long countStudentTextbookRead() {
        return countStudentTextbookRead(null, null);
    }
    @Override
    public Long countStudentTextbookRead(String startTime, String endTime) {
        List<StudentTextbookCompletionReturnParam> completionList;
        if(startTime == null && endTime == null){
            completionList = countStudentTextbookCompetion();
        }else {
            completionList = countStudentTextbookCompetion(startTime,endTime);
        }

        //统计完成度为指定值的教材数量
        long completedTextbooks = completionList.stream()
                .filter(param -> param.getCompletion() != null && param.getCompletion() >= COMPLETION_THRESHOLD)
                .count();
        return completedTextbooks;
    }

//    public Long countStudentTextbookReadByTime(String start_time,String end_time) {
//        List<StudentTextbookCompletionReturnParam> completionList = countStudentTextbookCompetionByTime(start_time,end_time);
//
//        //统计完成度为指定值的教材数量
//        long completedTextbooks = completionList.stream()
//                .filter(param -> param.getCompletion() != null && param.getCompletion() >= COMPLETION_THRESHOLD)
//                .count();
//        return completedTextbooks;
//    }

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
    /**
     * 根据时间段计算学生的教材阅读时长
     * @param  startTime 开始时间
     * @param  endTime   结束时间
     * @return 学生的教材阅读总时长
     */
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
     * 学生个人学习路径
     */
    @Override
    public StudentStudyPathReturnParam countStudentStudyPath() {
        Long userId = UserUtils.get().getId();
        StudentStudyPathReturnParam returnParam = new StudentStudyPathReturnParam();
        returnParam.setTextbookReadNum(studentDataStatisticsMapper.countTextbookByUserId(userId));
        returnParam.setFavoriteTextbookNum(studentDataStatisticsMapper.countFavoritebookByUserId(userId));
        returnParam.setCompletionNum(this.countStudentTextbookRead());
        returnParam.setTextbookReadingTime(this.countStudentTextbookReadingTime());

        List<StudentTextbookReadingTimeTopParam> readingTimeTopList = calculateTextbookReadingTimeTop(userId);
        returnParam.setStudentTextbookReadingTimeTop(readingTimeTopList);
        return returnParam;
    }

    @Override
    public StudentBehaviorReturnParam analyzeStudentBehavior(String startTime, String endTime) {
        Long userId = UserUtils.get().getId();
        StudentBehaviorReturnParam result = new StudentBehaviorReturnParam();
        // 1. 分析阅读时长分布
        List<Map<String, Object>> readingData = studentDataStatisticsMapper.groupReadingTimeByDay(userId, startTime, endTime);
        double readingVariance = calculateVariance(readingData);

        // 2. 分析笔记数量分布
        List<Map<String, Object>> noteData = studentDataStatisticsMapper.groupNotesByDay(userId, startTime, endTime);
        double noteVariance = calculateVariance(noteData);

        // 3. 分析答题次数分布
        List<Map<String, Object>> questionData = studentDataStatisticsMapper.groupQuestionsByDay(userId, startTime, endTime);
        double questionVariance = calculateVariance(questionData);

        // 4. 综合分析
        double averageVariance = (readingVariance + noteVariance + questionVariance) / 3.0;
        double score;
        if (averageVariance == 0) {
            score = 100; // 方差为0时得满分
        } else {
            // 使用指数衰减函数，可以根据需要调整衰减系数0.1
            score = 100 * Math.exp(-0.1 * averageVariance);

            // 确保分数在0-100范围内
            score = Math.max(0, Math.min(100, score));
        }

        result.setHabitType(getBehaviorType(averageVariance));
        result.setRegularityScore(score);
        return result;
    }

    @Override
    public StudentAnalysisReturnParam countStudentPersonalAnalysis(String startTime, String endTime) {
        Long userId = UserUtils.get().getId();
        StudentAnalysisReturnParam returnParam = new StudentAnalysisReturnParam();
        //统计学生起止时间阅读时长
        returnParam.setReadingTime(this.countStudentTextbookReadingTimeByTime(startTime, endTime));
        //统计学生起止时间阅读数量
        returnParam.setReadingNum(studentDataStatisticsMapper.countStudentTextbookReadByTime(userId,startTime, endTime));
        //统计学生起止时间完成教材阅读数量
        returnParam.setCompletionReadingNum(this.countStudentTextbookRead(startTime, endTime));
        //统计学生起止时间笔记数量
        returnParam.setNotesNum(this.countStudentNotesByTime(startTime, endTime));
        //统计学生起止时间答题题库数量
        returnParam.setQuestionBankNum(this.countStudentQuestionsByTime(startTime, endTime));
        //统计学生起止时间交流反馈数量
        returnParam.setCommunicationFeedbackNum(this.countStudentCommunicationFeedbackByTime(startTime, endTime));
        //统计学生书架教材数量
        returnParam.setShelfBookNum(this.countStudentFavoritebook());

        //统计学生课程教材数量
        List<Long> courseTextbookIds = studentDataStatisticsMapper.listStudentCourseTextbookIds(userId);
        returnParam.setMyCourseBookNum((long) courseTextbookIds.size());

        List<StudentTextbookCompletionReturnParam> allCompletionInfo = this.countStudentTextbookCompetion(startTime, endTime);
        Set<Long> completedTextbookIds = allCompletionInfo.stream()
                .filter(param -> param.getCompletion() != null && param.getCompletion() >= COMPLETION_THRESHOLD)
                .map(StudentTextbookCompletionReturnParam::getTextbookId)
                .collect(Collectors.toSet());

        // 3. 计算课程教材中已完成的数量 (交集)
        long completedCourseBookCount = 0;
        if (courseTextbookIds != null && !completedTextbookIds.isEmpty()) {
            completedCourseBookCount = courseTextbookIds.stream()
                    .filter(completedTextbookIds::contains)
                    .count();
        }
        returnParam.setMyCourseBookCompletionNum(completedCourseBookCount);

        return returnParam;
    }

    @Override
    public StudentTextbookSituationReturnParam countStudentTextbookSituation(Long textbookId) {
        Long userId = UserUtils.get().getId();
        StudentTextbookSituationReturnParam returnParam = new StudentTextbookSituationReturnParam();
        // 【优化】直接调用SQL进行高效计算，避免在Java中加载大量数据
        returnParam.setStudyCount(studentDataStatisticsMapper.countStudySessionsByTextbook(userId, textbookId));

        //统计最后阅读的目录id
        returnParam.setLastReadingCatalogueId(studentDataStatisticsMapper.findLastReadingCatalogueId(userId, textbookId));

        //阅读过的目录id列表
        returnParam.setReadingCatalogueIdList(studentDataStatisticsMapper.findCatalogueIdList(userId,textbookId));

        // 计算该教材的阅读总时长（分钟）
        returnParam.setReadingDurationMinutes(calculateReadingDurationMinutesByTextbook(userId, textbookId));

        return returnParam;
    }

    @Override
    public List<StudentTextbookRankParam> countStudentTextbookReadingRank() {
        Long userId = UserUtils.get().getId();
        //从数据库获取该用户的所有学习日志记录
        List<LearningLog> records = studentDataStatisticsMapper.findAddDatetime(userId);

        if (records == null || records.size() < 2) {
            return new ArrayList<>();
        }

        // 定义时间差的容忍范围，用于判断是否为连续阅读
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;

        // 使用Map按教材ID分组统计有效阅读时长
        // Key: textbookId, Value: readingTime
        Map<Long, Long> textbookReadingTimeMap = new HashMap<>();

        for (int i = 0; i < records.size() - 1; i++) {
            LearningLog currentLog = records.get(i);
            LearningLog nextLog = records.get(i + 1);

            if (currentLog.getAddDatetime() == null || nextLog.getAddDatetime() == null) {
                continue;
            }

            // 必须是同一本教材的连续记录才能计算时长
            if (!Objects.equals(currentLog.getTextbookId(), nextLog.getTextbookId())) {
                continue;
            }

            // 计算两条记录之间的时间差
            Duration duration = Duration.between(currentLog.getAddDatetime(), nextLog.getAddDatetime());
            long seconds = duration.getSeconds();

            // 如果时间差在预设范围内，则视为有效阅读，时长+1
            if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                Long textbookId = currentLog.getTextbookId();
                textbookReadingTimeMap.put(textbookId, textbookReadingTimeMap.getOrDefault(textbookId, 0L) + 1);
            }
        }

        //将统计结果从Map转换为List<StudentTextbookRankParam>
        List<StudentTextbookRankParam> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : textbookReadingTimeMap.entrySet()) {
            Long textbookId = entry.getKey();
            Long readingTime = entry.getValue();

            // 根据教材ID获取教材信息
            Textbook textbook = studentDataStatisticsMapper.getTextbookById(textbookId);
            String textbookName = (textbook != null) ? textbook.getTextbookName() : "未知教材";

            // 创建并填充排行参数对象
            result.add(new StudentTextbookRankParam()
                    .setTextbook_id(textbookId)
                    .setTextbook_name(textbookName)
                    .setRead_time(readingTime));
        }

        // 按阅读时长（read_time）进行降序排序
        result.sort(Comparator.comparingLong(StudentTextbookRankParam::getRead_time).reversed());

        return result;
    }

    private Long calculateReadingDurationMinutesByTextbook(Long userId, Long textbookId) {
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;
        List<LearningLog> logs = studentDataStatisticsMapper.findAddDatetime(userId);
        if (logs == null) {
            return 0L;
        }
        // 仅保留该教材的日志，并确保按时间升序
        List<LearningLog> textbookLogs = logs.stream()
                .filter(l -> Objects.equals(l.getTextbookId(), textbookId))
                .sorted(Comparator.comparing(LearningLog::getAddDatetime, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        if (textbookLogs.size() < 2) {
            return 0L;
        }
        long minutes = 0L;
        for (int i = 0; i < textbookLogs.size() - 1; i++) {
            LearningLog a = textbookLogs.get(i);
            LearningLog b = textbookLogs.get(i + 1);
            LocalDateTime t1 = a.getAddDatetime();
            LocalDateTime t2 = b.getAddDatetime();
            if (t1 == null || t2 == null) {
                continue;
            }
            long seconds = Duration.between(t1, t2).getSeconds();
            if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                minutes += 1;
            }
        }
        return minutes;
    }

    /**
     * 计算给定数据集的方差
     * @param data List of maps, each map contains 'count'
     * @return a double value of variance
     */
    private double calculateVariance(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }

        double[] values = data.stream()
                .map(d -> ((Number) d.get("count")).doubleValue())
                .mapToDouble(Double::doubleValue)
                .toArray();

        if (values.length <= 1) {
            return 0.0; // 单个数据点或没有数据，方差为0
        }

        Variance variance = new Variance();
        return variance.evaluate(values);
    }

    /**
     * 根据方差值判断学习行为类型
     * @param variance 方差
     * @return 行为类型描述
     */
    private String getBehaviorType(double variance) {
        // 这些阈值是示例，需要根据实际业务数据进行调整以达到最佳效果
        final double CRAMMING_THRESHOLD = 10.0; // 突击型学习的方差阈值
        final double UNIFORM_THRESHOLD = 2.0;   // 均匀型学习的方差阈值

        if (variance > CRAMMING_THRESHOLD) {
            return "突击型学习";
        } else if (variance <= UNIFORM_THRESHOLD) {
            return "均匀型学习";
        } else {
            return "波动型学习"; // 介于两者之间
        }
    }

    /**
     * 计算各教材阅读时长及占比
     * @param userId 用户ID
     * @return 各教材阅读时长及占比列表
     */
    private List<StudentTextbookReadingTimeTopParam> calculateTextbookReadingTimeTop(Long userId) {
        // 获取学习日志记录
        List<LearningLog> records = studentDataStatisticsMapper.findAddDatetime(userId);

        if (records == null || records.size() < 2) {
            return new ArrayList<>();
        }

        // 容忍范围
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;

        // 按教材ID分组统计阅读时间
        Map<Long, Long> textbookReadingTimeMap = new HashMap<>();

        for (int i = 0; i < records.size() - 1; i++) {
            LearningLog currentLog = records.get(i);
            LearningLog nextLog = records.get(i + 1);

            if (currentLog.getAddDatetime() == null || nextLog.getAddDatetime() == null) {
                continue;
            }

            // 检查是否为同一教材
            if (!Objects.equals(currentLog.getTextbookId(), nextLog.getTextbookId())) {
                continue;
            }

            Duration duration = Duration.between(currentLog.getAddDatetime(), nextLog.getAddDatetime());
            long seconds = duration.getSeconds();

            // 符合条件的时间间隔计为1秒阅读时间
            if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                Long textbookId = currentLog.getTextbookId();
                textbookReadingTimeMap.put(textbookId, textbookReadingTimeMap.getOrDefault(textbookId, 0L) + 1);
            }
        }

        // 计算总阅读时间
        long totalTime = textbookReadingTimeMap.values().stream().mapToLong(Long::longValue).sum();

        // 构造返回结果
        List<StudentTextbookReadingTimeTopParam> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : textbookReadingTimeMap.entrySet()) {
            Long textbookId = entry.getKey();
            Long readingTime = entry.getValue();

            // 获取教材名称
            Textbook textbook = studentDataStatisticsMapper.getTextbookById(textbookId);
            String textbookName = (textbook != null) ? textbook.getTextbookName() : "未知教材";

            // 计算占比
            Long proportion = (totalTime > 0) ? Math.round((double) readingTime / totalTime * 100) : 0L;

            result.add(new StudentTextbookReadingTimeTopParam()
                    .setTextbookName(textbookName)
                    .setReadingTime(readingTime)
                    .setProportion(proportion));
        }

        // 按阅读时长降序排序，取前10条
        return result.stream()
                .sorted((a, b) -> b.getReadingTime().compareTo(a.getReadingTime()))
                .limit(10)
                .collect(Collectors.toList());
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
     * 根据学生ID查询阅读过的教材，按阅读量排名返回
     * @param studentId 学生ID
     * @return 教材阅读排行榜列表
     */
    @Override
    public List<StudentTextbookRankParam> countStudentTextbookReadingRankByStudentId(Long studentId) {
        // 先根据studentId获取userId
        Student student = studentMapper.selectById(studentId);
        if (student == null || student.getUserId() == null) {
            return new ArrayList<>(); // 学生不存在或没有关联用户
        }
        
        Long userId = student.getUserId();
        
        //从数据库获取该用户的所有学习日志记录
        List<LearningLog> records = studentDataStatisticsMapper.findAddDatetime(userId);

        if (records == null || records.size() < 2) {
            return new ArrayList<>();
        }

        // 定义时间差的容忍范围，用于判断是否为连续阅读
        final long MIN_DIFF_SECONDS = 55;
        final long MAX_DIFF_SECONDS = 65;

        // 使用Map按教材ID分组统计有效阅读时长
        // Key: textbookId, Value: readingTime
        Map<Long, Long> textbookReadingTimeMap = new HashMap<>();

        for (int i = 0; i < records.size() - 1; i++) {
            LearningLog currentLog = records.get(i);
            LearningLog nextLog = records.get(i + 1);

            if (currentLog.getAddDatetime() == null || nextLog.getAddDatetime() == null) {
                continue;
            }

            // 必须是同一本教材的连续记录才能计算时长
            if (!Objects.equals(currentLog.getTextbookId(), nextLog.getTextbookId())) {
                continue;
            }

            // 计算两条记录之间的时间差
            Duration duration = Duration.between(currentLog.getAddDatetime(), nextLog.getAddDatetime());
            long seconds = duration.getSeconds();

            // 如果时间差在预设范围内，则视为有效阅读，时长+1
            if (seconds >= MIN_DIFF_SECONDS && seconds <= MAX_DIFF_SECONDS) {
                Long textbookId = currentLog.getTextbookId();
                textbookReadingTimeMap.put(textbookId, textbookReadingTimeMap.getOrDefault(textbookId, 0L) + 1);
            }
        }

        //将统计结果从Map转换为List<StudentTextbookRankParam>
        List<StudentTextbookRankParam> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : textbookReadingTimeMap.entrySet()) {
            Long textbookId = entry.getKey();
            Long readingTime = entry.getValue();

            // 根据教材ID获取教材信息
            Textbook textbook = studentDataStatisticsMapper.getTextbookById(textbookId);
            String textbookName = (textbook != null) ? textbook.getTextbookName() : "未知教材";

            // 创建并填充排行参数对象
            result.add(new StudentTextbookRankParam()
                    .setTextbook_id(textbookId)
                    .setTextbook_name(textbookName)
                    .setRead_time(readingTime));
        }

        // 按阅读时长（read_time）进行降序排序
        result.sort(Comparator.comparingLong(StudentTextbookRankParam::getRead_time).reversed());

        return result;
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
    
    /**
     * 根据班级名称和学生姓名分页查询学生阅读排名
     * @param groupName 班级名称
     * @param studentName 学生姓名
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    public Page<StudentReadingRankParam> getStudentReadingRankByPage(String groupName, String studentName, Long current, Long size) {
        Long currentUserId = UserUtils.get().getId();
        // 获取当前用户信息
        SysTbuser currentUser = sysUserMapper.selectById(currentUserId);
        
        // 获取有权限的班级列表
        List<Group> authorizedGroups = getGroupsByTeacherUserId(currentUserId);
        Set<Long> authorizedGroupIds = authorizedGroups.stream().map(Group::getId).collect(Collectors.toSet());
        
        // 如果没有权限访问任何班级，返回空结果
        if (authorizedGroupIds.isEmpty()) {
            Page<StudentReadingRankParam> emptyPage = new Page<>(current, size);
            emptyPage.setRecords(new ArrayList<>());
            emptyPage.setTotal(0L);
            return emptyPage;
        }
        
        // 构造查询条件
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        if (studentName != null && !studentName.isEmpty()) {
            queryWrapper.like(Student::getName, studentName);
        }
        
        // 只查询有权限的班级中的学生
        queryWrapper.in(Student::getClassId, authorizedGroupIds);
        
        // 分页查询学生
        Page<Student> studentPage = new Page<>(current, size);
        Page<Student> pageResult = studentMapper.selectPage(studentPage, queryWrapper);
        
        List<StudentReadingRankParam> result = new ArrayList<>();
        // 处理每个学生的信息
        for (Student student : pageResult.getRecords()) {
            Long classId = student.getClassId();
            // 检查班级是否在授权列表中
            if (!authorizedGroupIds.contains(classId)) {
                continue;
            }
            
            // 获取班级信息
            Group group = groupMapper.selectById(classId);
            if (group == null) {
                continue;
            }
            
            // 检查班级名称是否匹配
            if (groupName != null && !groupName.isEmpty() && !groupName.equals(group.getName())) {
                continue;
            }
            
            StudentReadingRankParam param = new StudentReadingRankParam();
            param.setStudentId(student.getId())
                 .setStudentName(student.getName())
                 .setGroupId(group.getId())
                 .setGroupName(group.getName());
            
            // 计算该学生的教材阅读数量
            Long readingCount = studentDataStatisticsMapper.countTextbookByUserId(student.getUserId());
            param.setReadingCount(readingCount == null ? 0L : readingCount);
            
            result.add(param);
        }
        
        // 根据阅读量排序并设置排名
        result.sort((a, b) -> b.getReadingCount().compareTo(a.getReadingCount()));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank((long) (i + 1));
        }
        
        // 构造返回的分页结果
        Page<StudentReadingRankParam> finalPage = new Page<>(current, size);
        finalPage.setRecords(result);
        finalPage.setTotal(pageResult.getTotal());
        
        return finalPage;
    }
    
    /**
     * 根据教师用户ID获取有权限的班级列表
     * 管理员可查看所有班级，教师只能查看自己负责的班级
     * @param userId 用户ID
     * @return 班级列表
     */
    private List<Group> getGroupsByTeacherUserId(Long userId) {
        // 检查用户是否为管理员（userType == 0 表示管理员）
        SysTbuser currentUser = sysUserService.getById(userId);
        if (currentUser != null && currentUser.getUserType() != null && currentUser.getUserType() == 0) {
            // 如果是管理员，返回所有班级
            return groupMapper.selectList(new LambdaQueryWrapper<Group>().eq(Group::getStatus, 1));
        }
        
        // 1. 根据用户ID获取教师ID
        Long teacherId = teacherMapper.getTeacherIdByUserId(userId);
        if (teacherId == null) {
            return new ArrayList<>(); // 如果找不到对应的教师，返回空列表
        }
        
        // 2. 创建用于存储班级ID的Set，以实现去重
        Set<Long> groupIds = new HashSet<>();
        
        // 3. 根据教师ID在group表中查找对应的班级
        LambdaQueryWrapper<Group> groupQueryWrapper = new LambdaQueryWrapper<>();
        groupQueryWrapper.eq(Group::getTeacherId, teacherId)
                .eq(Group::getStatus, 1); // 只查找状态为1的班级
        List<Group> groupsByTeacher = groupMapper.selectList(groupQueryWrapper);
        
        // 4. 收集这些班级的ID
        groupsByTeacher.forEach(group -> groupIds.add(group.getId()));
        
        // 5. 根据教师ID查找该教师对应的课程ID
        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.eq(Course::getTeacherId, teacherId);
        List<Course> courses = courseService.list(courseQueryWrapper);
        List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());
        
        // 6. 如果存在课程，根据课程ID查找对应的班级ID
        if (!courseIds.isEmpty()) {
            // 通过course_class_list表查找班级ID
            LambdaQueryWrapper<CourseClassList> courseClassListQueryWrapper = new LambdaQueryWrapper<>();
            courseClassListQueryWrapper.in(CourseClassList::getCourseId, courseIds);
            List<CourseClassList> courseClassLists = courseClassListService.list(courseClassListQueryWrapper);
            
            // 收集班级ID
            courseClassLists.forEach(courseClassList -> groupIds.add(courseClassList.getClassId()));
        }
        
        // 7. 根据收集到的班级ID查找所有班级信息
        if (!groupIds.isEmpty()) {
            LambdaQueryWrapper<Group> finalGroupQueryWrapper = new LambdaQueryWrapper<>();
            finalGroupQueryWrapper.in(Group::getId, groupIds)
                    .eq(Group::getStatus, 1); // 只查找状态为1的班级
            return groupMapper.selectList(finalGroupQueryWrapper);
        }
        
        return groupsByTeacher;
    }
    
    @Override
    public Double getStudentScoreRate(Long userId) {
        // 通过userId查询对应的studentId
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
        if (student == null) {
            return 0.0;
        }
        Long studentId = student.getId();
        return studentDataStatisticsMapper.getStudentScoreRate(studentId);
    }
}
