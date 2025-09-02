package com.upc.modular.datastatistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.upc.modular.materials.mapper.TeachingMaterialsMapper;
import com.upc.modular.materials.service.ITeachingMaterialsService;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.service.ITextbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Map<String, Object> getStudentDetailStatistics(Long studentId) {
        // TODO: 实现学生详细统计数据逻辑
        return null;
    }

    @Override
    public Integer getTodayVisitors() {
        // TODO: 实现今日访问人数统计逻辑
        return null;
    }

    @Override
    public List<Map<String, Object>> getVisitorsByTime(Integer days) {
        // TODO: 实现按时间统计访问人数逻辑
        return null;
    }

    @Override
    public Long getTodayStudyDuration() {
        // TODO: 实现今日总学习时长统计逻辑
        return null;
    }

    @Override
    public List<Map<String, Object>> getStudyDurationByTime(Integer days) {
        // TODO: 实现按时间统计总学习时长逻辑
        return null;
    }

    @Override
    public Integer getTodayActiveUsers() {
        // TODO: 实现今日活跃人数统计逻辑
        return null;
    }

    @Override
    public List<Map<String, Object>> getActiveUsersByTime(Integer days) {
        // TODO: 实现按时间统计活跃人数逻辑
        return null;
    }

    @Override
    public Long getStudentCount() {
        // TODO: 实现学生数量统计逻辑
        return null;
    }

    @Override
    public Long getTeacherCount() {
        // TODO: 实现教师数量统计逻辑
        return teacherService.count();
    }

    @Override
    public Long getIdeologicalEducationCount() {
        // TODO: 实现教学思政数量统计逻辑
        return null;
    }

    @Override
    public Long getTeachingActivitiesCount() {
        // TODO: 实现教学活动数量统计逻辑
        return null;
    }

    @Override
    public Long getQuestionBankCount() {
        // TODO: 实现题库数量统计逻辑
        return null;
    }

    @Override
    public Long getClassCount() {
        // TODO: 实现班级数量统计逻辑
        return null;
    }

    @Override
    public Long getTeachingCourseCount() {
        // TODO: 实现在授课程数量统计逻辑
        return null;
    }

    @Override
    public Long getSmartTextbookCount() {
        // TODO: 实现智慧教材数量统计逻辑

        return null;
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
            String type = (String) row.get("type");
            Long count = ((Number) row.get("cnt")).longValue();
            typeCountMap.put(type, count);
        }

        return typeCountMap;
    }


    @Override
    public Long getCommunicationFeedbackCount() {
        // TODO: 实现交流反馈数量统计逻辑
        return null;
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
