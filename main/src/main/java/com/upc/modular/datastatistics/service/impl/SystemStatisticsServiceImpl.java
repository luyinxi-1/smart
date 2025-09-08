package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.course.service.ICourseService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SystemStatisticsServiceImpl  implements ISystemStatisticsService {

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

    @Override
    public Long getTodayVisitorCount() {
        return systemDataStatisticsMapper.getTodayVisitorCount();
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


    @Override
    public List<Map<String, Object>> getStudyDurationByTime(Map<String, Object> param) {
        // TODO: 实现按时间统计总学习时长逻辑
        return systemDataStatisticsMapper.getStudyDurationByTime(param);
    }
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
            // Deleted: String type = row.get("type").toString(); // 使用toString()方法代替强制类型转换
            Object typeObj = row.get("type");
            String type = (typeObj != null) ? typeObj.toString() : "未知类型"; // 添加空值检查
            Long count = ((Number) row.get("cnt")).longValue();
            typeCountMap.put(type, count);
        }
        return typeCountMap;
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
