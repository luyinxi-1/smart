package com.upc.modular.questionbank.entity;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.upc.modular.questionbank.mapper.TeachingQuestionClassificationMapper;
import com.upc.modular.questionbank.service.ITeachingQuestionService;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

// 监听器不能被 Spring 管理，需要 new 出来，或者通过构造方法传入 Service
public class QuestionImportListener extends AnalysisEventListener<QuestionImportDTO> {

    // 假设这是每次批量入库的数量
    private static final int BATCH_COUNT = 100;

    // 缓存数据列表
    private List<TeachingQuestion> cachedDataList = new ArrayList<>(BATCH_COUNT);

    // 外部传入的 Service 和 上下文参数
    private final ITeachingQuestionService questionService;
    private final Long textbookId;
    private final String textbookName;
    private final Long chapterId;
    private final String chapterName;
    private TeachingQuestionClassificationMapper teachingQuestionClassificationMapper;

    public QuestionImportListener(ITeachingQuestionService questionService,
                                  Long textbookId, String textbookName,
                                  Long chapterId, String chapterName, TeachingQuestionClassificationMapper teachingQuestionClassificationMapper) {
        this.questionService = questionService;
        this.textbookId = textbookId;
        this.textbookName = textbookName;
        this.chapterId = chapterId;
        this.chapterName = chapterName;
        this.teachingQuestionClassificationMapper = teachingQuestionClassificationMapper;
    }

    @Override
    public void invoke(QuestionImportDTO data, AnalysisContext context) {
        // 1. 数据校验与过滤
        // 过滤掉Excel最后的说明行（根据你的模板，最后一行是说明）
        if (StringUtils.isBlank(data.getTypeName()) || data.getTypeName().contains("表头中红色")) {
            return;
        }

        // 2. 将 DTO 转换为 实体对象
        TeachingQuestion question = convertToEntity(data);

        // 3. 填充教材和章节信息 (复用原有逻辑)
        question.setTextbookId(textbookId);
        question.setTextbookName(textbookName);
        question.setChapterId(chapterId);
        question.setChapterName(chapterName);

        // 构建选项的JSON格式
        String choiceQuestionOptions = buildOptionsJson(data);
        question.setChoiceQuestionOptions(choiceQuestionOptions);


        cachedDataList.add(question);

        // 4. 达到批次大小，保存一次
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 保存剩余的数据
        saveData();
    }

    private void saveData() {
        if (!cachedDataList.isEmpty()) {
            questionService.saveBatch(cachedDataList); // 使用 MyBatis-Plus 的批量保存
            cachedDataList = new ArrayList<>(BATCH_COUNT);
        }
    }

    /**
     * 构建选项的JSON格式
     */
    private String buildOptionsJson(QuestionImportDTO data) {
        List<Option> options = new ArrayList<>();
        options.add(new Option("A", data.getOptionA()));
        options.add(new Option("B", data.getOptionB()));
        options.add(new Option("C", data.getOptionC()));
        options.add(new Option("D", data.getOptionD()));

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            // 如果转换失败，回退到原来的分号分隔格式
            return data.getOptionA() + ";" + data.getOptionB() + ";" + data.getOptionC() + ";" + data.getOptionD();
        }
    }

    /**
     * 选项内部类
     */
    private static class Option {
        private String value;
        private String content;

        public Option(String value, String content) {
            this.value = value;
            this.content = content;
        }

        // Getters and setters
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * 核心转换逻辑：将 Excel 的中文映射为数据库的 ID 或 Code
     */
    private TeachingQuestion convertToEntity(QuestionImportDTO dto) {
        TeachingQuestion q = new TeachingQuestion();
        q.setContent(dto.getContent());
        q.setAnswerAnalysis(dto.getAnalysis());
        q.setTeachingQuestionClassificationId(getClassificationIdByName(dto.getAttribute()));
        // --- 题型转换 (示例) ---
        // 建议建立一个 Map 或 Enum 来维护这种映射
        switch (dto.getTypeName()) {
            case "单选题": q.setType(1); break;
            case "多选题": q.setType(2); break;
            case "判断题": q.setType(3); break;
            case "填空题": q.setType(4); break;
            case "简答题": q.setType(5); break;
            case "计算题": q.setType(6); break;
            case "论述题": q.setType(7); break;
            // ... 其他类型
            default: q.setType(0); // 默认或错误类型
        }

        // --- 难度转换 (示例) ---
        if (dto.getDifficultyName() != null) {
            if (dto.getDifficultyName().contains("易")) q.setDifficulty(1);
            else if (dto.getDifficultyName().contains("中")) q.setDifficulty(2);
            else if (dto.getDifficultyName().contains("难")) q.setDifficulty(3);
        }


        String formattedAnswer = formatAnswer(dto.getTypeName(), dto.getAnswer());
        q.setAnswer(formattedAnswer);

        return q;
    }

    /**
     * 根据分类名称获取分类ID
     */
    private Long getClassificationIdByName(String classificationName) {
        if (classificationName == null || classificationName.isEmpty()) {
            return null;
        }
        
        // 根据分类名称模糊查询获取对应的ID
        // 使用Spring注入的方式获取Mapper
        // 注意：在实际使用中，需要通过适当的方式注入TeachingQuestionClassificationMapper
        // 这里假设已经通过某种方式获得了mapper实例
        try {
            // 创建查询条件
            TeachingQuestionClassification condition = new TeachingQuestionClassification();
            condition.setTeachingQuestionClassificationName(classificationName);

             TeachingQuestionClassification result = teachingQuestionClassificationMapper.selectOne(new QueryWrapper<>(condition));
             if (result != null) {
                 return result.getId();
             }
            
            // 临时返回null，直到实现真正的查询逻辑
            return null;
        } catch (Exception e) {
            // 发生异常时返回null
            return null;
        }
    }

    private String formatAnswer(String typeName, String rawAnswer) {
        if (StringUtils.isBlank(rawAnswer)) {
            return "";
        }

        // 1. 处理判断题
        if ("判断题".equals(typeName)) {
            // 兼容各种写法：正确/错误, 对/错, T/F, Y/N
            if (rawAnswer.contains("正确") || rawAnswer.contains("对") || "T".equalsIgnoreCase(rawAnswer) || "Y".equalsIgnoreCase(rawAnswer)) {
                return "1";
            }
            return "0";
        }

        // 2. 处理多选题 和 填空题 (需要转 JSON 数组)
        // 假设你的逻辑是：多选题和填空题都需要存为 ["A","B"] 这种格式
        if ("多选题".equals(typeName) || "填空题".equals(typeName)) {
            try {
                // A. 统一分隔符：将中文逗号 "，" 替换为英文逗号 ","
                String normalized = rawAnswer.replace("，", ",");

                // B. 分割、去空格、过滤空串
                List<String> answerList = Arrays.stream(normalized.split(","))
                        .map(String::trim)                // 去除首尾空格 " A " -> "A"
                        .filter(StringUtils::isNotBlank)  // 过滤掉空的项
                        .collect(Collectors.toList());

                // C. 转换为 JSON 字符串
                return new ObjectMapper().writeValueAsString(answerList);

            } catch (JsonProcessingException e) {
                // 如果转换失败，记录日志或保留原样
                return rawAnswer;
            }
        }

        // 3. 单选题或其他 (直接返回 "A" 或 "B")
        return rawAnswer.trim();
    }
}