package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.upc.modular.datastatistics.controller.param.ChapterMasteryVO;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.course.service.ICourseService;
import com.upc.modular.datastatistics.controller.param.VisitorCountDTO;
import com.upc.modular.datastatistics.mapper.SystemDataStatisticsMapper;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import com.upc.modular.group.service.IGroupService;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.questionbank.service.ITeachingQuestionService;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.teachingactivities.service.IDiscussionTopicReplyService;
import com.upc.modular.teachingactivities.service.IDiscussionTopicService;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.service.IIdeologicalMaterialService;
import com.upc.modular.textbook.service.ITextbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SystemStatisticsServiceImpl implements ISystemStatisticsService {

    @Autowired
    private ITeachingMaterialsService teachingMaterialsService;

    @Autowired
    private TeachingMaterialsMapper teachingMaterialsMapper;
    @Autowired
    private ITeacherService teacherService;
    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private ICourseService courseService;
    @Autowired
    private IGroupService groupService;
    @Autowired
    private ITeachingQuestionService teachingQuestionService;
    @Autowired
    private IDiscussionTopicService discussionTopicService;
    @Autowired
    private IIdeologicalMaterialService ideologicalMaterialService;
    @Autowired
    private IStudentService studentService;
    @Autowired
    private IDiscussionTopicReplyService discussionTopicReplyService;

    @Autowired
    private SystemDataStatisticsMapper systemDataStatisticsMapper;
    // 添加SysLogMapper的依赖
    @Autowired
    private com.upc.modular.auth.mapper.SysLogMapper userLoginLogMapper;

    //今日访问人数
    @Override
    public Long getTodayVisitorCount() {

        return systemDataStatisticsMapper.getTodayVisitorCount();
    }

    //按时间统计访问人数



    @Override
    public List<VisitorCountDTO> getStudentVisitorCountByTime(String startDate, String endDate) {
        // 1. (可选但推荐) 参数校验
        if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
            throw new IllegalArgumentException("开始日期和结束日期不能为空！");
        }

        // 校验日期格式和顺序
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDate start;
        LocalDate end;

        // 判断输入是日期格式还是日期时间格式
        if (startDate.contains(":")) {
            // 如果包含时间部分，使用日期时间格式解析
            start = LocalDate.parse(startDate, dateTimeFormatter);
        } else {
            // 否则使用日期格式解析
            start = LocalDate.parse(startDate, dateFormatter);
        }

        if (endDate.contains(":")) {
            // 如果包含时间部分，使用日期时间格式解析
            end = LocalDate.parse(endDate, dateTimeFormatter);
        } else {
            // 否则使用日期格式解析
            end = LocalDate.parse(endDate, dateFormatter);
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期！");
        }

        // 2. 准备参数
        // 为了兼容数据库可能需要 'yyyy-MM-dd HH:mm:ss' 格式
        // KingBase/PostgreSQL 的 BETWEEN 对 'yyyy-MM-dd' 兼容性很好，但补全时间是更严谨的做法
        String startDateTime = startDate + (startDate.contains(":") ? "" : " 00:00:00");
        String endDateTime = endDate + (endDate.contains(":") ? "" : " 23:59:59");

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDateTime);
        params.put("endDate", endDateTime);

        // 3. 调用 Mapper 并返回结果
        return systemDataStatisticsMapper.getStudentVisitorCountByTime(params);
    }


    @Override
    public List<Map<String, Object>> getVisitorCountByTime(Map<String, Object> param) {
        return systemDataStatisticsMapper.getVisitorCountByTime(param);
    }

    @Override
    public Long getTodayStudyDuration() {
        // TODO: 实现今日总学习时长统计逻辑
        return systemDataStatisticsMapper.getTodayStudyDuration();
    }

    // === 修改后的代码 ===
    @Override
    public Long getStudyDurationByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: 可以在这里添加参数校验，例如 endTime 必须大于 startTime
        return systemDataStatisticsMapper.getStudyDurationByTimeRange(startTime, endTime);
    }
/*    @Override
    public List<Map<String, Object>> getStudyDurationByTime(Map<String, Object> param) {
        // TODO: 实现按时间统计总学习时长逻辑
        return systemDataStatisticsMapper.getStudyDurationByTime(param);
    }*/
/*    @Override
    public List<Map<String, Object>> getActiveUserCountByTime(Map<String, Object> param) {
        // TODO: 实现按时间统计活跃人数逻辑
        return systemDataStatisticsMapper.getActiveUserCountByTime(param);
    }

    @Override
    public Long getTodayActiveUserCount() {
        // TODO: 实现今日活跃人数统计逻辑
        return systemDataStatisticsMapper.getTodayActiveUserCount();
    }*/
    @Override
    public Long getStudentCount() {
        // TODO: 实现学生数量统计逻辑
        return studentService.count();
    }

    @Override
    public Long getTeacherCount() {
        // TODO: 实现教师数量统计逻辑
        return teacherService.count();
    }

    @Override
    public Long getIdeologicalEducationCount() {
        // TODO: 实现教学思政数量统计逻辑
        return ideologicalMaterialService.count();
    }

    @Override
    public Long getTeachingActivitiesCount() {
        // TODO: 实现教学活动数量统计逻辑
        return discussionTopicService.count();
    }

    @Override
    public Long getQuestionBankCount() {     //teaching_question表
        // TODO: 实现题库数量统计逻辑
        return teachingQuestionService.count() ;
    }

    @Override
    public Long getClassCount() {
        // TODO: 实现班级数量统计逻辑
        return groupService.count();
    }

    @Override
    public Long getTeachingCourseCount() {
        // TODO: 实现在授课程数量统计逻辑
        return courseService.count();
    }

    @Override
    public Long getSmartTextbookCount() {
        // TODO: 实现智慧教材数量统计逻辑

        return textbookService.count();
    }

    //教材类型统计
    @Override
    public Map<String, Long> getTextbookTypeCount() {
        // 使用 MyBatis-Plus 的链式查询 + 分组统计
        List<Map<String, Object>> result = textbookService.listMaps(
                new QueryWrapper<Textbook>()
                        .select("type, COUNT(*) as cnt")
                        .groupBy("type")
        );

        // 转换成 Map<String, Long>
        Map<String, Long> typeCountMap = new HashMap<>();
        for (Map<String, Object> row : result) {
            Object typeObj = row.get("type");
            String type = (typeObj != null) ? typeObj.toString() : "未知类型"; // 添加空值检查
            Long count = ((Number) row.get("cnt")).longValue();
            typeCountMap.put(type, count);
        }
        return typeCountMap;
    }
    
    /**
     * 处理时间参数，确保符合业务需求：
     * 1. 如果startTime只提供了日期，默认设置为当天00:00:00
     * 2. 如果endTime只提供了日期，默认设置为当天23:59:59
     * 3. 如果提供了完整时间，则使用用户提供的具体时间
     * 4. endTime必须大于等于startTime
     * 5. 如果endTime晚于今天，就默认为今天
     * 6. 如果不输入，就默认为所有时间
     * 
     * @param params 参数Map
     */
    private void processTimeParams(Map<String, Object> params) {
        if (params == null) {
            return;
        }

        String startTimeStr = null;
        String endTimeStr = null;

        // 获取原始参数
        Object startTimeObj = params.get("startTime");
        Object endTimeObj = params.get("endTime");

        // 处理startTime参数
        if (startTimeObj instanceof String) {
            startTimeStr = ((String) startTimeObj).trim();
            if (startTimeStr.isEmpty()) {
                startTimeStr = null;
            }
        }

        // 处理endTime参数
        if (endTimeObj instanceof String) {
            endTimeStr = ((String) endTimeObj).trim();
            if (endTimeStr.isEmpty()) {
                endTimeStr = null;
            }
        }

        LocalDate today = LocalDate.now();
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        // 解析开始时间
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            try {
                // 尝试解析完整的时间格式 "yyyy-MM-dd HH:mm:ss"
                if (startTimeStr.length() == 19 && startTimeStr.charAt(4) == '-' && startTimeStr.charAt(13) == ':') {
                    startDate = LocalDate.parse(startTimeStr.substring(0, 10));
                } 
                // 尝试解析日期格式 "yyyy-MM-dd"
                else if (startTimeStr.length() == 10 && startTimeStr.charAt(4) == '-') {
                    startDate = LocalDate.parse(startTimeStr);
                } 
                // 其他格式尝试解析
                else {
                    startDate = LocalDate.parse(startTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception e) {
                log.warn("Invalid startTime format: {}", startTimeStr);
                // 格式错误则忽略该参数
                startDate = null;
            }
        }

        // 解析结束时间
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            try {
                // 尝试解析完整的时间格式 "yyyy-MM-dd HH:mm:ss"
                if (endTimeStr.length() == 19 && endTimeStr.charAt(4) == '-' && endTimeStr.charAt(13) == ':') {
                    endDate = LocalDate.parse(endTimeStr.substring(0, 10));
                } 
                // 尝试解析日期格式 "yyyy-MM-dd"
                else if (endTimeStr.length() == 10 && endTimeStr.charAt(4) == '-') {
                    endDate = LocalDate.parse(endTimeStr);
                } 
                // 其他格式尝试解析
                else {
                    endDate = LocalDate.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception e) {
                log.warn("Invalid endTime format: {}", endTimeStr);
                // 格式错误则忽略该参数
                endDate = null;
            }
        }

        // 如果没有提供开始时间，默认不限制开始时间
        if (startDate == null) {
            params.put("startTime", null);
        } else {
            // 如果只提供了日期，设置为当天的00:00:00
            if (startTimeStr != null && startTimeStr.length() <= 10) {
                params.put("startTime", startDate.toString() + " 00:00:00");
            } else {
                params.put("startTime", startTimeStr);
            }
        }

        // 如果没有提供结束时间，默认不限制结束时间
        if (endDate == null) {
            params.put("endTime", null);
        } else {
            // 如果结束时间晚于今天，设置为今天
            if (endDate.isAfter(today)) {
                endDate = today;
            }

            // 如果开始时间不为空且结束时间早于开始时间，设置结束时间等于开始时间
            if (startDate != null && endDate.isBefore(startDate)) {
                endDate = startDate;
            }

            // 如果只提供了日期，设置为当天的23:59:59
            if (endTimeStr != null && endTimeStr.length() <= 10) {
                params.put("endTime", endDate.toString() + " 23:59:59");
            } else {
                // 如果提供了完整时间，则使用用户提供的具体时间
                params.put("endTime", endTimeStr);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getTextbookReadingRank(Map<String, Object> params) {
        // 处理时间参数
        processTimeParams(params);
        return systemDataStatisticsMapper.getTextbookReadingRank(params);
    }

    @Override
    public List<Map<String, Object>> getTextbookTypeReadingRank(Map<String, Object> params) {
        // 处理时间参数
        processTimeParams(params);
        return systemDataStatisticsMapper.getTextbookTypeReadingRank(params);
    }

    @Override
    public List<ChapterMasteryVO> getStudentChapterMastery(Long studentId, Long textbookId) {
        List<Map<String, Object>> rawData = systemDataStatisticsMapper.getStudentChapterMastery(studentId, textbookId);
        List<ChapterMasteryVO> result = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("#.##");

        for (Map<String, Object> item : rawData) {
            ChapterMasteryVO vo = new ChapterMasteryVO();
            vo.setChapterId((Long) item.get("chapterId"));

            // 清理章节名称中的HTML标签
            String chapterName = (String) item.get("chapterName");
            if (chapterName != null) {
                // 移除HTML标签
                chapterName = chapterName.replaceAll("<[^>]+>", "");
                // 处理HTML实体
                chapterName = chapterName.replace("&nbsp;", " ");
                chapterName = chapterName.replace("&amp;", "&");
            }
            vo.setChapterName(chapterName);

            // 检查章节是否有题目
            Object questionCountObj = item.get("questionCount");
            Long questionCount = (questionCountObj instanceof Number) ? ((Number) questionCountObj).longValue() : 0L;

            if (questionCount == null || questionCount == 0) {
                // 该章节没有题目
                vo.setMasteryPercentage("0");
                vo.setMasteryDisplay("该章节没有题目");
            } else {
                // 章节有题目，检查是否有答题记录
                Object masteryPercentageObj = item.get("masteryPercentage");
                Double masteryPercentage = (masteryPercentageObj instanceof Number) ? ((Number) masteryPercentageObj).doubleValue() : -1.0;

                if (masteryPercentage != null && masteryPercentage >= 0) {
                    String percentageStr = df.format(masteryPercentage);
                    vo.setMasteryPercentage(percentageStr);
                    vo.setMasteryDisplay(percentageStr + "%");
                } else {
                    vo.setMasteryPercentage("0");
                    vo.setMasteryDisplay("暂无做题记录");
                }
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public Long getCommunicationFeedbackCount() {
        // TODO: 实现交流反馈数量统计逻辑
        return discussionTopicReplyService.count();
    }

    @Override
    public Long getTeachingMaterialsCount() {
        // TODO: 实现教学素材数量统计逻辑
        return teachingMaterialsService.count();
    }
    // TODO:资源使用数据统计
    @Override
    public Map<String, Object> getResourceUsageStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. 总数
        Long totalCount = teachingMaterialsService.count();

        // 2. 公共资源数量
        Long publicCount = teachingMaterialsService.lambdaQuery()
                .eq(TeachingMaterials::getIsPublic, true)
                .count();

        // 3. 私有资源数量
        Long privateCount = totalCount - publicCount;

        // 4. 按类型统计（假设 TeachingMaterials 有 type 字段）
        List<Map<String, Object>> typeStatistics =teachingMaterialsMapper.selectMaps(
                new QueryWrapper<TeachingMaterials>()
                        .select("type, COUNT(*) as count")
                        .groupBy("type")
        );

        // 5. 总文件大小（假设 TeachingMaterials 有 size 字段，单位 KB）
        Double totalSize = teachingMaterialsService.lambdaQuery()
                .select(TeachingMaterials::getFileSize)
                .list()
                .stream()
                .filter(tm -> tm != null && tm.getFileSize() != null) // 过滤掉fileSize为null的对象
                .mapToDouble(TeachingMaterials::getFileSize)
                .sum();

        result.put("totalCount", totalCount);
        result.put("publicCount", publicCount);
        result.put("privateCount", privateCount);
        result.put("typeStatistics", typeStatistics);
        result.put("totalSizeMB", totalSize);

        return result;
    }
}