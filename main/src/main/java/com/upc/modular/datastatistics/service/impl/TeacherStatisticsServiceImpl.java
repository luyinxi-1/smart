package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.datastatistics.controller.param.*;
import com.upc.modular.datastatistics.entity.TeacherStatistics;
import com.upc.modular.datastatistics.mapper.TeacherStatisticsMapper;
import com.upc.modular.datastatistics.service.ITeacherStatisticsService;
import com.upc.modular.datastatistics.service.IStudentDataStatistics;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.entity.UserClassList;
import com.upc.modular.group.service.IGroupService;
import com.upc.modular.group.service.IUserClassListService;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师统计Service实现类
 */
@Service
public class TeacherStatisticsServiceImpl extends ServiceImpl<TeacherStatisticsMapper, TeacherStatistics> implements ITeacherStatisticsService {

    @Autowired
    private TeacherStatisticsMapper teacherStatisticsMapper;

    @Autowired
    private ITeacherService teacherService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IUserClassListService userClassListService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IStudentDataStatistics studentDataStatistics;

    @Autowired
    private ISystemStatisticsService systemStatisticsService;

    @Override
    public TeacherStatisticsReturnParam getTeacherPersonalStatistics(Long teacherId) {
        TeacherStatisticsReturnParam result = new TeacherStatisticsReturnParam();
        result.setTeacherId(teacherId);

        // 获取教师姓名
        try {
            TeacherReturnVo teacherInfo = teacherService.getInformationByTeacherId(teacherId);
            if (teacherInfo != null) {
                result.setTeacherName(teacherInfo.getName());
            }
        } catch (Exception e) {
            result.setTeacherName("未知教师");
        }

        // 统计各项数据
        result.setClassCount(countTeacherClasses(teacherId));
        result.setStudentCount(countTeacherStudents(teacherId));
        result.setTextbookCount(countTeacherTextbooks(teacherId));
        result.setCourseCount(countTeacherCourses(teacherId));
        result.setStatisticsDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return result;
    }

    @Override
    public Integer countTeacherClasses(Long teacherId) {
        return teacherStatisticsMapper.countTeacherClasses(teacherId);
    }

    @Override
    public Integer countTeacherStudents(Long teacherId) {
        return teacherStatisticsMapper.countTeacherStudents(teacherId);
    }

    @Override
    public Integer countTeacherTextbooks(Long teacherId) {
        return teacherStatisticsMapper.countTeacherTextbooks(teacherId);
    }

    @Override
    public Integer countTeacherCourses(Long teacherId) {
        return teacherStatisticsMapper.countTeacherCourses(teacherId);
    }

    @Override
    public void saveTeacherStatistics(TeacherStatistics statistics) {
        statistics.setStatisticsDate(LocalDateTime.now());
        save(statistics);
    }

    // ========== 智能化分析功能实现 ==========

    @Override
    public ClassChapterMasteryReturnParam analyzeClassChapterMastery(Long classId, Long textbookId) {
        ClassChapterMasteryReturnParam result = new ClassChapterMasteryReturnParam();

        // 获取班级信息
        Group group = groupService.getById(classId);
        if (group == null) {
            throw new RuntimeException("班级不存在");
        }

        result.setClassId(classId);
        result.setClassName(group.getName());
        result.setTextbookId(textbookId);

        // 获取班级中的学生列表
        List<Long> studentIds = teacherStatisticsMapper.getClassStudentIds(classId);
        result.setTotalStudents(studentIds.size());

        if (studentIds.isEmpty()) {
            return result;
        }

        // 获取教材信息
        result.setTextbookName("教材名称");

        // 分析每个学生的章节掌握度
        List<ClassChapterMasteryReturnParam.ChapterMasteryDetail> chapterDetails = new ArrayList<>();

        // 获取教材的所有章节
        List<ChapterMasteryVO> allChapters = systemStatisticsService.getStudentChapterMastery(studentIds.get(0), textbookId);

        for (ChapterMasteryVO chapter : allChapters) {
            ClassChapterMasteryReturnParam.ChapterMasteryDetail detail = new ClassChapterMasteryReturnParam.ChapterMasteryDetail();
            detail.setChapterId(chapter.getChapterId());
            detail.setChapterName(chapter.getChapterName());

            // 统计该章节所有学生的掌握度
            List<Double> masteryScores = new ArrayList<>();
            int excellentCount = 0, goodCount = 0, needImprovementCount = 0;

            for (Long studentId : studentIds) {
                List<ChapterMasteryVO> studentChapters = systemStatisticsService.getStudentChapterMastery(studentId, textbookId);
                ChapterMasteryVO studentChapter = studentChapters.stream()
                        .filter(c -> c.getChapterId().equals(chapter.getChapterId()))
                        .findFirst()
                        .orElse(null);

                if (studentChapter != null && !"-1".equals(studentChapter.getMasteryPercentage())) {
                    try {
                        double score = Double.parseDouble(studentChapter.getMasteryPercentage());
                        masteryScores.add(score);

                        if (score >= 90) excellentCount++;
                        else if (score >= 70) goodCount++;
                        else needImprovementCount++;
                    } catch (NumberFormatException e) {
                        // 忽略无效的分数
                    }
                }
            }

            // 计算班级平均掌握度
            if (!masteryScores.isEmpty()) {
                double averageMastery = masteryScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                detail.setAverageMastery(averageMastery);
                detail.setMasteryLevel(getMasteryLevel(averageMastery));
            } else {
                detail.setAverageMastery(0.0);
                detail.setMasteryLevel("无数据");
            }

            detail.setExcellentStudents(excellentCount);
            detail.setGoodStudents(goodCount);
            detail.setNeedImprovementStudents(needImprovementCount);

            chapterDetails.add(detail);
        }

        result.setChapterDetails(chapterDetails);
        result.setParticipatingStudents((int) chapterDetails.stream()
                .mapToInt(detail -> detail.getExcellentStudents() + detail.getGoodStudents() + detail.getNeedImprovementStudents())
                .sum());

        return result;
    }

    @Override
    public ClassAnalysisReturnParam generateClassAnalysisReport(Long classId, String startTime, String endTime) {
        ClassAnalysisReturnParam result = new ClassAnalysisReturnParam();

        // 获取班级信息
        Group group = groupService.getById(classId);
        if (group == null) {
            throw new RuntimeException("班级不存在");
        }

        result.setClassId(classId);
        result.setClassName(group.getName());
        result.setTimeRange(startTime + " 至 " + endTime);

        // 获取班级中的学生列表
        List<Long> studentIds = teacherStatisticsMapper.getClassStudentIds(classId);
        result.setTotalStudents(studentIds.size());

        if (studentIds.isEmpty()) {
            return result;
        }

        // 使用真实的数据库查询统计数据
        Long totalReadingTime = teacherStatisticsMapper.getClassTotalReadingTime(classId, startTime, endTime);
        Long totalReadingNum = teacherStatisticsMapper.getClassTotalReadingNum(classId, startTime, endTime);
        Long totalNotesNum = teacherStatisticsMapper.getClassTotalNotesNum(classId, startTime, endTime);
        Long totalQuestionBankNum = teacherStatisticsMapper.getClassTotalQuestionBankNum(classId, startTime, endTime);
        Long totalCommunicationFeedbackNum = teacherStatisticsMapper.getClassTotalCommunicationFeedbackNum(classId, startTime, endTime);

        // 计算完成教材阅读数量
        Long totalCompletionReadingNum = totalReadingNum;

        result.setTotalReadingTime(totalReadingTime);
        result.setAverageReadingTime(studentIds.isEmpty() ? 0 : totalReadingTime / studentIds.size());
        result.setTotalReadingNum(totalReadingNum);
        result.setAverageReadingNum(studentIds.isEmpty() ? 0 : totalReadingNum / studentIds.size());
        result.setTotalCompletionReadingNum(totalCompletionReadingNum);
        result.setAverageCompletionReadingNum(studentIds.isEmpty() ? 0 : totalCompletionReadingNum / studentIds.size());
        result.setTotalNotesNum(totalNotesNum);
        result.setAverageNotesNum(studentIds.isEmpty() ? 0 : totalNotesNum / studentIds.size());
        result.setTotalQuestionBankNum(totalQuestionBankNum);
        result.setAverageQuestionBankNum(studentIds.isEmpty() ? 0 : totalQuestionBankNum / studentIds.size());
        result.setTotalCommunicationFeedbackNum(totalCommunicationFeedbackNum);
        result.setAverageCommunicationFeedbackNum(studentIds.isEmpty() ? 0 : totalCommunicationFeedbackNum / studentIds.size());

        return result;
    }

    @Override
    public ClassBehaviorAnalysisReturnParam analyzeClassLearningBehavior(Long classId, String startTime, String endTime) {
        ClassBehaviorAnalysisReturnParam result = new ClassBehaviorAnalysisReturnParam();

        // 获取班级信息
        Group group = groupService.getById(classId);
        if (group == null) {
            throw new RuntimeException("班级不存在");
        }

        result.setClassId(classId);
        result.setClassName(group.getName());

        // 获取班级中的学生列表
        List<Long> studentIds = teacherStatisticsMapper.getClassStudentIds(classId);
        result.setTotalStudents(studentIds.size());

        if (studentIds.isEmpty()) {
            return result;
        }

        // 分析每个学生的学习行为
        List<ClassBehaviorAnalysisReturnParam.StudentBehaviorDetail> studentDetails = new ArrayList<>();
        List<Double> regularityScores = new ArrayList<>();
        int excellentCount = 0, goodCount = 0, needImprovementCount = 0;

        for (Long studentId : studentIds) {
            ClassBehaviorAnalysisReturnParam.StudentBehaviorDetail detail = new ClassBehaviorAnalysisReturnParam.StudentBehaviorDetail();
            detail.setStudentId(studentId);

            // 获取学生姓名
            Student student = studentService.getById(studentId);
            detail.setStudentName(student != null ? student.getName() : "未知学生");

            // 分析学生学习行为（这里需要调用学生行为分析，但需要修改为支持指定学生ID）
            // 暂时使用模拟数据，实际应该调用学生数据统计服务
            double regularityScore = 75.0 + Math.random() * 25.0; // 模拟分数
            String habitType = getBehaviorType(regularityScore);

            detail.setRegularityScore(regularityScore);
            detail.setHabitType(habitType);

            regularityScores.add(regularityScore);

            if (regularityScore >= 85) excellentCount++;
            else if (regularityScore >= 70) goodCount++;
            else needImprovementCount++;

            studentDetails.add(detail);
        }

        // 计算班级整体规律性分数
        double classRegularityScore = regularityScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        result.setClassRegularityScore(classRegularityScore);
        result.setClassHabitType(getBehaviorType(classRegularityScore));

        result.setExcellentHabitStudents(excellentCount);
        result.setGoodHabitStudents(goodCount);
        result.setNeedImprovementStudents(needImprovementCount);
        result.setStudentDetails(studentDetails);

        // 生成分析建议
        result.setRecommendation(generateBehaviorRecommendation(classRegularityScore, excellentCount, goodCount, needImprovementCount));

        return result;
    }

    // ========== 辅助方法 ==========

    /**
     * 根据掌握度分数获取等级
     */
    private String getMasteryLevel(double score) {
        if (score >= 90) return "优秀";
        else if (score >= 80) return "良好";
        else if (score >= 70) return "及格";
        else if (score >= 60) return "待提高";
        else return "需要加强";
    }

    /**
     * 根据规律性分数获取行为类型
     */
    private String getBehaviorType(double score) {
        if (score >= 85) return "规律型学习者";
        else if (score >= 70) return "稳定型学习者";
        else if (score >= 60) return "波动型学习者";
        else return "需要改善学习习惯";
    }

    /**
     * 生成行为分析建议
     */
    private String generateBehaviorRecommendation(double classScore, int excellent, int good, int needImprovement) {
        StringBuilder recommendation = new StringBuilder();

        if (classScore >= 80) {
            recommendation.append("班级整体学习习惯良好，建议继续保持。");
        } else if (classScore >= 70) {
            recommendation.append("班级学习习惯基本稳定，建议加强个别学生的指导。");
        } else {
            recommendation.append("班级学习习惯需要改善，建议制定统一的学习计划。");
        }

        if (needImprovement > 0) {
            recommendation.append("有").append(needImprovement).append("名学生需要重点关注学习习惯的培养。");
        }

        return recommendation.toString();
    }

    @Override
    public IPage<TeacherTextbookPopularityParam> getTeacherTextbookPopularity(Page<TeacherTextbookPopularityParam> page, Long teacherId) {
        IPage<Map<String, Object>> rawDataPage = teacherStatisticsMapper.getTeacherTextbookPopularity(page, teacherId);
        List<TeacherTextbookPopularityParam> resultList = new ArrayList<>();
        long rankStart = (page.getCurrent() - 1) * page.getSize() + 1;

        for (Map<String, Object> data : rawDataPage.getRecords()) {
            TeacherTextbookPopularityParam param = new TeacherTextbookPopularityParam();
            param.setRank((int) rankStart++);
            param.setTextbookId(((Number) data.get("textbookId")).longValue());
            param.setTextbookName((String) data.get("textbookName"));
            param.setReaderCount(((Number) data.get("readerCount")).longValue());
            param.setReadingDurationMinutes(((Number) data.get("readingDurationMinutes")).longValue());
            param.setTeachingActivityCount(((Number) data.get("teachingActivityCount")).longValue());
            param.setCommunicationFeedbackCount(((Number) data.get("communicationFeedbackCount")).longValue());
            param.setPopularityScore(((Number) data.get("popularityScore")).intValue());
            resultList.add(param);
        }

        IPage<TeacherTextbookPopularityParam> resultPage = new Page<>(rawDataPage.getCurrent(), rawDataPage.getSize(), rawDataPage.getTotal());
        resultPage.setRecords(resultList);
        return resultPage;
    }

    @Override
    public List<TeacherTextbookPopularityParam> exportTeacherTextbookPopularity(Long teacherId) {
        List<Map<String, Object>> rawData = teacherStatisticsMapper.getTeacherTextbookPopularityForExport(teacherId);
        List<TeacherTextbookPopularityParam> result = new ArrayList<>();
        int rank = 1;

        for (Map<String, Object> data : rawData) {
            TeacherTextbookPopularityParam param = new TeacherTextbookPopularityParam();
            param.setRank(rank++);
            param.setTextbookId(((Number) data.get("textbookId")).longValue());
            param.setTextbookName((String) data.get("textbookName"));
            param.setReaderCount(((Number) data.get("readerCount")).longValue());
            param.setReadingDurationMinutes(((Number) data.get("readingDurationMinutes")).longValue());
            param.setTeachingActivityCount(((Number) data.get("teachingActivityCount")).longValue());
            param.setCommunicationFeedbackCount(((Number) data.get("communicationFeedbackCount")).longValue());
            param.setPopularityScore(((Number) data.get("popularityScore")).intValue());
            result.add(param);
        }

        return result;
    }
}