package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.questionbank.controller.param.AnswerDetailDTO;
import com.upc.modular.questionbank.controller.param.StudentExercisesRecordPageSearchParam;
import com.upc.modular.questionbank.controller.param.SubmitAnswerRequest;
import com.upc.modular.questionbank.mapper.TeachingQuestionBankMapper;
import com.upc.modular.questionbank.entity.*;
import com.upc.modular.questionbank.mapper.*;
import com.upc.modular.questionbank.service.IStudentExercisesRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
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
    private TeacherExercisesRecordMapper teacherExercisesRecordMapper;

    @Autowired
    private StudentExercisesContentMapper studentExercisesContentMapper;
    @Autowired
    private TeacherExercisesContentMapper teacherExercisesContentMapper;

    @Autowired
    private TeachingQuestionBankMapper teachingQuestionBankMapper;

    @Autowired
    private TeachingQuestionMapper teachingQuestionMapper;

    @Autowired
    private QuestionsBanksListMapper questionsBanksListMapper;

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentFinalGradeMapper studentFinalGradeMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional // 保证整个提交和判卷过程是原子性的
    public Long submitAnswers(Long userId, SubmitAnswerRequest request) {
        // --- 流程 1.1: 前置校验 ---
        // 1. 校验题库是否存在
        TeachingQuestionBank bank = teachingQuestionBankMapper.selectById(request.getBankId());
        if (bank == null) {
            throw new RuntimeException("题库不存在！");
        }

        // 判断用户角色
        SysTbuser user = sysUserMapper.selectById(userId);
        boolean isTeacher = user != null && user.getUserType() != null && user.getUserType() == 2;

        if (isTeacher) {
            // 教师答题逻辑
            LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teacher::getUserId, userId);
            Teacher teacher = teacherMapper.selectOne(queryWrapper);
            if (teacher == null) {
                throw new RuntimeException("教师信息不存在！");
            }
            long teacherId = teacher.getId();

            // 教师答题不校验作答次数

            // 创建教师答卷记录
            TeacherExercisesRecord record = new TeacherExercisesRecord();
            record.setTeacherId(teacherId);
            record.setTeachingQuestionBankId(request.getBankId());
            // record.setExerciseNum((int) attemptCount + 1); // 教师可忽略此字段
            record.setScore(null);
            record.setStatus(1); // 1-待批改
            teacherExercisesRecordMapper.insert(record);
            Long recordId = record.getId();

            // 保存教师答题明细
            for (AnswerDetailDTO answerDTO : request.getAnswers()) {
                TeacherExercisesContent content = new TeacherExercisesContent();
                content.setRecordId(recordId);
                content.setTeacherId(teacherId);
                content.setTeachingQuestion(answerDTO.getTeachingQuestionId());
                content.setContent(answerDTO.getStudentAnswer());
                content.setScore(null);
                content.setTeachingQuestionBankId(request.getBankId());
                teacherExercisesContentMapper.insert(content);
            }
            // 教师答题后暂不自动判卷
            return recordId;

        } else {
            // 学生答题逻辑
            //获取学生ID
            LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Student::getUserId,userId);
            Student student = studentMapper.selectOne(queryWrapper);
            if (student == null) {
                throw new RuntimeException("学生信息不存在！");
            }
            long studentId = student.getId();
            // --- 【核心修改】2. 校验作答次数 ---
            LambdaQueryWrapper<StudentExercisesRecord> qw = new LambdaQueryWrapper<>();
            qw.eq(StudentExercisesRecord::getStudentId, studentId)
                    .eq(StudentExercisesRecord::getTeachingQuestionBankId, request.getBankId());
            long attemptCount = studentExercisesRecordMapper.selectCount(qw);

            if (bank.getIsLimitAttempts() != null && bank.getIsLimitAttempts() == 1) {
                // 只有在开启限制的情况下，才执行次数校验
                // 只有当 max_attempts 字段不为null时，限制才真正生效
                if (bank.getMaxAttempts() != null && attemptCount >= bank.getMaxAttempts()) {
                    throw new RuntimeException("作答次数已达上限！");
                }
            }
            // 如果 bank.getIsAttemptsLimit() 为 false 或为 null，则直接跳过整个次数校验逻辑。
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
            if (question == null) {
                continue; // 如果题目不存在，跳过
            }

            // 根据题目类型进行判卷
            // 主观题只标记，不评分
            if (question.getType() == 5) { // --- 问答题 ---
                hasSubjective = true;
                continue; // 跳过，不自动评分
            }

            // --- 以下为客观题判卷逻辑 ---

            // 客观题必须有标准答案才能评分
            if (question.getAnswer() == null) {
                continue;
            }

            // 获取这道题的满分值
            double fullScore = questionScoreMap.getOrDefault(question.getId(), 0.0);
            double score = 0d; // 本题最终得分，默认为0

            // 统一处理学生答案和标准答案，去除首尾空格，便于比较
            String studentAnswer = (content.getContent() != null) ? content.getContent().trim() : "";
            String correctAnswer = question.getAnswer().trim();

            switch (question.getType()) {
                case 1: // --- 单选题 ---
                    if (correctAnswer.equalsIgnoreCase(studentAnswer)) {
                        score = fullScore;
                        content.setResult("单选题正确");
                    } else {
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
            }

            // 更新单题得分
            content.setScore(score);
            studentExercisesContentMapper.updateById(content);
        }

        // --- 流程 3: 更新答卷最终状态和总分 ---
        if (!hasSubjective) {
            // 所有客观题已判完，直接从内存中的列表计算总分，避免重复查询
            double totalScore = contents.stream()
                    .filter(c -> c.getScore() != null)
                    .mapToDouble(StudentExercisesContent::getScore)
                    .sum();

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

    @Override
    public Void deleteStudentExercisesRecordByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 批量查询
        List<StudentExercisesRecord> found = studentExercisesRecordMapper.selectBatchIds(idList);
        // 如果数量不一致，则说明有遗漏
        if (found.size() != idList.size()) {
            // 找出那些不存在的 ID
            List<Long> foundIds = found.stream()
                    .map(StudentExercisesRecord::getId)
                    .collect(Collectors.toList());
            List<Long> missing = idList.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "未找到对应的学生做题记录 ID：" + missing
            );
        }
        
        // 先删除关联的答题内容记录
        studentExercisesContentMapper.delete(
            new LambdaQueryWrapper<StudentExercisesContent>()
                .in(StudentExercisesContent::getRecordId, idList)
        );
        
        // 再删除主表记录
        this.removeByIds(idList);

        return null;
    }

    @Override
    public void updateStudentExercisesRecord(StudentExercisesRecord param) {
        Long studentExercisesRecordId = param.getId();
        if (studentExercisesRecordId == null) {
            throw new RuntimeException("更新失败，未提供学生做题记录ID！");
        }

        StudentExercisesRecord oldstudentExercisesRecord = this.getById(studentExercisesRecordId);
        if (oldstudentExercisesRecord == null) {
            throw new RuntimeException("ID为 " + studentExercisesRecordId + " 的学生做题记录不存在，无法更新！");
        }

        Long studentId = param.getStudentId();
        if (studentId != null) {
            if (!studentId.equals(oldstudentExercisesRecord.getStudentId())) {
                boolean isStudentExists = studentMapper.exists(
                        new LambdaQueryWrapper<Student>().eq(Student::getId, studentId)
                );
                if (!isStudentExists) {
                    throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + studentId + " 的学生不存在！");
                }
            }
        }

        Long teachingQuestionBankId = param.getTeachingQuestionBankId();
        if (teachingQuestionBankId != null) {
            if (!teachingQuestionBankId.equals(oldstudentExercisesRecord.getTeachingQuestionBankId())) {
                boolean isQuestionBankExists = teachingQuestionBankMapper.exists(
                        new LambdaQueryWrapper<TeachingQuestionBank>().eq(TeachingQuestionBank::getId, teachingQuestionBankId)
                );
                if (!isQuestionBankExists) {
                    throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + teachingQuestionBankId + " 的题目所属题库不存在！");
                }
            }
        }

        this.updateById(param);
    }

    @Override
    public Page<StudentExercisesRecord> selectStudentExercisesRecordPage(StudentExercisesRecordPageSearchParam param) {
        Page<StudentExercisesRecord> page = new Page<>(param.getCurrent(), param.getSize());
        return studentExercisesRecordMapper.selectStudentExercisesRecordPage(page, param);
    }

    @Override
    public void inserStudentExercisesRecord(StudentExercisesRecord param) {
        // 参数非空校验
        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "学生做题记录参数不能为空");
        }

        // 获取外键ID，并进行空值校验
        Long studentId = param.getStudentId();
        Long teachingQuestionBankId = param.getTeachingQuestionBankId();

        if (studentId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "学生ID不能为空");
        }
        if (teachingQuestionBankId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "题目所属题库ID不能为空");
        }

        // 校验 studentId
        LambdaQueryWrapper<Student> studentQueryWrapper = new LambdaQueryWrapper<>();
        studentQueryWrapper.eq(Student::getId, studentId);
        boolean isStudentExists = studentMapper.exists(studentQueryWrapper);
        if (!isStudentExists) {
            throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + studentId + " 的学生不存在！");
        }

        // 校验 teachingQuestionBankId
        LambdaQueryWrapper<TeachingQuestionBank> questionBankQueryWrapper = new LambdaQueryWrapper<>();
        questionBankQueryWrapper.eq(TeachingQuestionBank::getId, teachingQuestionBankId);
        boolean isQuestionBankExists = teachingQuestionBankMapper.exists(questionBankQueryWrapper);
        if (!isQuestionBankExists) {
            throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + teachingQuestionBankId + " 的题目所属题库不存在！");
        }

        this.save(param);
    }
}
