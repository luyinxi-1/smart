package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.modular.questionbank.controller.param.AnswerDetailDTO;
import com.upc.modular.questionbank.controller.param.SubmitAnswerRequest;
import com.upc.modular.questionbank.mapper.TeachingQuestionBankMapper;
import com.upc.modular.questionbank.entity.*;
import com.upc.modular.questionbank.mapper.*;
import com.upc.modular.questionbank.service.IStudentExercisesRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Service
public class StudentExercisesRecordServiceImpl extends ServiceImpl<StudentExercisesRecordMapper, StudentExercisesRecord> implements IStudentExercisesRecordService {

    @Autowired
    private StudentExercisesRecordMapper studentExercisesRecordMapper;

    @Autowired
    private StudentExercisesContentMapper studentExercisesContentMapper;

    @Autowired
    private TeachingQuestionBankMapper teachingQuestionBankMapper;

    @Autowired
    private TeachingQuestionMapper teachingQuestionMapper;

    @Autowired
    private QuestionsBanksListMapper questionsBanksListMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private StudentFinalGradeMapper studentFinalGradeMapper;

    @Override
    @Transactional // 保证整个提交和判卷过程是原子性的
    public Long submitAnswers(Long userId, SubmitAnswerRequest request) {
        // --- 流程 1.1: 前置校验 ---
        // 1. 校验题库是否存在
        TeachingQuestionBank bank = teachingQuestionBankMapper.selectById(request.getBankId());
        if (bank == null) {
            throw new RuntimeException("题库不存在！");
        }

        //获取学生ID
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getUserId,userId);
        long studentId = studentMapper.selectOne(queryWrapper).getId();

        // 2. 校验作答次数
        LambdaQueryWrapper<StudentExercisesRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(StudentExercisesRecord::getStudentId, studentId)
                .eq(StudentExercisesRecord::getTeachingQuestionBankId, request.getBankId());
        long attemptCount = studentExercisesRecordMapper.selectCount(qw);
        if (bank.getMaxAttempts() != null && attemptCount >= bank.getMaxAttempts()) {
            throw new RuntimeException("作答次数已达上限！");
        }

        // --- 流程 1.2: 创建答卷记录 ---
        StudentExercisesRecord record = new StudentExercisesRecord();
        record.setStudentId(studentId);
        record.setTeachingQuestionBankId(request.getBankId());
        record.setExerciseNum((int) attemptCount + 1);
        record.setScore(null); // 初始总分为NULL
        // 初始状态先设为待批改，判完后再更新
        record.setStatus(1); // 1-待批改
        studentExercisesRecordMapper.insert(record);
        Long recordId = record.getId(); // 获取新生成的答卷ID

        // --- 流程 1.3: 保存答题明细 ---
        for (AnswerDetailDTO answerDTO : request.getAnswers()) {
            StudentExercisesContent content = new StudentExercisesContent();
            content.setRecordId(recordId);
            content.setStudentId(studentId);
            content.setTeachingQuestion(answerDTO.getTeachingQuestionId());
            content.setContent(answerDTO.getStudentAnswer());
            content.setScore(null); // 初始单题得分为NULL
            content.setTeachingQuestionBankId(request.getBankId());
            studentExercisesContentMapper.insert(content);
        }

        // --- 流程 2: 同步执行自动判卷 (未来可替换为MQ(消息队列)) ---
        autoJudgement(recordId);

        return recordId;
    }

    // 自动判卷的私有方法
    private void autoJudgement(Long recordId) {
        // 1. 获取答卷记录，我们需要从中得到 bankId
        StudentExercisesRecord record = studentExercisesRecordMapper.selectById(recordId);
        if (record == null) return;
        Long currentBankId = record.getTeachingQuestionBankId();

        // 2. 获取所有答题明细和题目信息
        List<StudentExercisesContent> contents = studentExercisesContentMapper.selectList(
                new LambdaQueryWrapper<StudentExercisesContent>().eq(StudentExercisesContent::getRecordId, recordId)
        );
        if (contents.isEmpty()) return;

        List<Long> questionIds = contents.stream()
                .map(StudentExercisesContent::getTeachingQuestion)
                .collect(Collectors.toList());

        // 3.一次性查出所有题目信息
        Map<Long, TeachingQuestion> questionMap = teachingQuestionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(TeachingQuestion::getId, q -> q));

        // 4. 一次性查出【当前这个题库下】所有题目的满分值
        LambdaQueryWrapper<QuestionsBanksList> scoreQueryWrapper = new LambdaQueryWrapper<>();
        scoreQueryWrapper
                .eq(QuestionsBanksList::getBankId, currentBankId) // 限定题库ID
                .in(QuestionsBanksList::getQuestionId, questionIds);

        Map<Long, Double> questionScoreMap = questionsBanksListMapper.selectList(scoreQueryWrapper)
                .stream()
                .collect(Collectors.toMap(
                        QuestionsBanksList::getQuestionId,
                        QuestionsBanksList::getScore,
                        (oldValue, newValue) -> newValue // 如果有重复，保留新的（理论上不应重复）
                ));

        boolean hasSubjective = false; // 标记是否有主观题

        // 2. 遍历判卷
        for (StudentExercisesContent content : contents) {
            TeachingQuestion question = questionMap.get(content.getTeachingQuestion());
            if (question == null || question.getAnswer() == null) {
                continue; // 如果题目或答案不存在，跳过
            }

            // 获取这道题的满分值
            double fullScore = questionScoreMap.getOrDefault(question.getId(), 0.0);
            double score = 0d; // 本题最终得分，默认为0

            // 统一处理学生答案和标准答案，去除首尾空格，便于比较
            String studentAnswer = (content.getContent() != null) ? content.getContent().trim() : "";
            String correctAnswer = question.getAnswer().trim();

            // 根据题目类型进行判卷
            switch (question.getType()) {
                case 1: // --- 单选题 ---
                    if (correctAnswer.equalsIgnoreCase(studentAnswer)) {  //.equalsIgnoreCase() 是 Java 中 String 类的一个方法，用于比较两个字符串的内容是否相同，同时忽略大小写差异
                        score = fullScore;
                        content.setResult("单选题正确");
                    }else{
                        content.setResult("单选题错误");
                    }
                    break;

                case 2: // --- 多选题 ---
                    // 假设答案格式为 "A,C,D"，无空格，按字母排序
                    // 1. 将学生答案和正确答案都处理成有序的字符列表，便于比较
                    List<String> studentChoices = Arrays.stream(studentAnswer.toUpperCase().split(","))
                            .filter(s -> !s.isEmpty())
                            .sorted()
                            .collect(Collectors.toList());
                    List<String> correctChoices = Arrays.stream(correctAnswer.toUpperCase().split(","))
                            .sorted()
                            .collect(Collectors.toList());

                    // 2. 判断是否完全正确
                    if (studentChoices.equals(correctChoices)) {
                        score = fullScore; // 完全一致，得满分
                        content.setResult("多选题题全对");
                    }
                    // 3. 判断是否漏选（学生答案是正确答案的子集）
                    else if (!studentChoices.isEmpty() && correctChoices.containsAll(studentChoices)) {
                        // containAll(A) 检查 B 是否包含 A 的所有元素
                        // 这里我们判断 正确答案列表 是否包含 学生答案列表 的所有元素
                        score = (int) Math.ceil(fullScore / 2.0); // 漏选，得一半分,向上取整
                        content.setResult("多选题漏选");
                    }else{
                        // 4. 其他情况（错选、多选）均为0分，score 默认就是0
                        content.setResult("多选题错选或多选");
                    }
                    break;

                case 3: // --- 判断题 ---
                    // 假设答案格式为 "true" / "false" 或 "1" / "0"
                    if (correctAnswer.equalsIgnoreCase(studentAnswer)) {
                        score = fullScore;
                        content.setResult("判断题正确");
                    }else{
                        content.setResult("判断题错误");
                    }
                    break;

                case 4: // --- 填空题 ---
                    // 假设多个空用 "|" 分隔
                    String[] studentFills = studentAnswer.split("\\|");
                    String[] correctFills = correctAnswer.split("\\|");

                    // 只有当填空数量完全一致时才给分
                    if (studentFills.length == correctFills.length) {
                        boolean allCorrect = true;
                        for (int i = 0; i < correctFills.length; i++) {
                            // 逐个对比答案，不区分大小写
                            if (!correctFills[i].trim().equalsIgnoreCase(studentFills[i].trim())) {
                                allCorrect = false;
                                break;
                            }
                        }
                        if (allCorrect) {
                            score = fullScore;
                        }
                    }
                    break;

                case 5: // --- 问答题 ---
                    hasSubjective = true;
                    continue; // 跳过，不自动评分
            }

            // 更新单题得分
            content.setScore(score);
            studentExercisesContentMapper.updateById(content);
        }

        // --- 流程 3: 更新答卷最终状态和总分 ---
        if (!hasSubjective) {
            // 所有客观题已判完，计算总分
            double totalScore = 0f;
            List<StudentExercisesContent> updatedContents = studentExercisesContentMapper.selectList(
                    new LambdaQueryWrapper<StudentExercisesContent>().eq(StudentExercisesContent::getRecordId, recordId)
            );
            for (StudentExercisesContent c : updatedContents) {
                if(c.getScore() != null) {
                    totalScore += c.getScore();
                }
            }

            // 更新答卷记录
            StudentExercisesRecord recordToUpdate = new StudentExercisesRecord();
            recordToUpdate.setId(recordId);

            recordToUpdate.setStatus(2); // 2-已完成
            recordToUpdate.setScore(totalScore);
            studentExercisesRecordMapper.updateById(recordToUpdate);

            // --- 流程 4: 触发最终成绩策略计算 (可以调用另一个Service) ---
            // calculateFinalScoreService.calculate(userId, bankId);
            this.calculateAndUpdateFinalGrade(recordId);
        }
        // 如果有主观题，答卷状态保持为1-待批改，等待教师判卷
    }


    @Override
    @Transactional
    public void calculateAndUpdateFinalGrade(Long recordId) {
        // 1. 获取刚刚完成的这份答卷的完整信息
        StudentExercisesRecord completedRecord = studentExercisesRecordMapper.selectById(recordId);
        if (completedRecord == null || completedRecord.getStatus() != 2) {
            // 如果答卷不存在或状态不是“已完成”，则不进行计算
            return;
        }
        Long studentId = completedRecord.getStudentId();
        Long bankId = completedRecord.getTeachingQuestionBankId();

        // 2. 获取题库的成绩策略
        TeachingQuestionBank bank = teachingQuestionBankMapper.selectById(bankId);
        if (bank == null || bank.getScorePolicy() == null) {
            return;
        }

        // 3. 查询该学生在该题库下所有“已完成”的答卷记录
        LambdaQueryWrapper<StudentExercisesRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(StudentExercisesRecord::getStudentId, studentId)
                .eq(StudentExercisesRecord::getTeachingQuestionBankId, bankId)
                .eq(StudentExercisesRecord::getStatus, 2)
                .isNotNull(StudentExercisesRecord::getScore);

        List<StudentExercisesRecord> allCompletedRecords = studentExercisesRecordMapper.selectList(qw);
        if (allCompletedRecords.isEmpty()) {
            return;
        }

        // 4. 根据策略找出最终有效的答卷记录
        Optional<StudentExercisesRecord> finalRecordOptional;
        // 假设 0:最高分, 1:最后一次
        if (bank.getScorePolicy() == 0) {
            finalRecordOptional = allCompletedRecords.stream()
                    .max(Comparator.comparing(StudentExercisesRecord::getScore));
        } else {
            finalRecordOptional = allCompletedRecords.stream()
                    .max(Comparator.comparing(StudentExercisesRecord::getAddDatetime));
        }

        // 5. 更新或插入最终成绩表
        if (finalRecordOptional.isPresent()) {
            StudentExercisesRecord finalRecord = finalRecordOptional.get();

            StudentFinalGrade finalGrade = new StudentFinalGrade();
            finalGrade.setStudentId(studentId);
            finalGrade.setBankId(bankId);
            finalGrade.setFinalScore(finalRecord.getScore());
            finalGrade.setRecordId(finalRecord.getId());
            finalGrade.setUpdateTime(LocalDateTime.now());

            // 采用 "UPSERT" 逻辑
            LambdaQueryWrapper<StudentFinalGrade> queryFinalGrade = new LambdaQueryWrapper<>();
            queryFinalGrade.eq(StudentFinalGrade::getStudentId, studentId)
                    .eq(StudentFinalGrade::getBankId, bankId);

            if (studentFinalGradeMapper.selectCount(queryFinalGrade) > 0) {
                // 已存在，更新
                studentFinalGradeMapper.update(finalGrade, queryFinalGrade);
            } else {
                // 不存在，插入
                studentFinalGradeMapper.insert(finalGrade);
            }
        }
    }
}
