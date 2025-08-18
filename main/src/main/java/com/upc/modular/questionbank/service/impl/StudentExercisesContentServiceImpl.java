package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.StudentExercisesContentPageSearchParam;
import com.upc.modular.questionbank.entity.StudentExercisesContent;
import com.upc.modular.questionbank.entity.StudentExercisesRecord;
import com.upc.modular.questionbank.entity.TeachingQuestion;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.upc.modular.questionbank.mapper.StudentExercisesContentMapper;
import com.upc.modular.questionbank.mapper.StudentExercisesRecordMapper;
import com.upc.modular.questionbank.mapper.TeachingQuestionBankMapper;
import com.upc.modular.questionbank.mapper.TeachingQuestionMapper;
import com.upc.modular.questionbank.service.IStudentExercisesContentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
public class StudentExercisesContentServiceImpl extends ServiceImpl<StudentExercisesContentMapper, StudentExercisesContent> implements IStudentExercisesContentService {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private TeachingQuestionMapper teachingQuestionMapper;
    @Autowired
    private StudentExercisesRecordMapper studentExercisesRecordMapper;
    @Autowired
    private TeachingQuestionBankMapper teachingQuestionBankMapper;
    @Autowired
    private StudentExercisesContentMapper studentExercisesContentMapper;
    @Override
    @Transactional // 保证数据操作的原子性
    public void inserStudentExercisesContent(StudentExercisesContent param) {
        // 参数非空校验
        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "学生做题内容参数不能为空");
        }

        // 获取外键ID，并进行空值校验
        Long studentId = param.getStudentId();
        Long teachingQuestionId = param.getTeachingQuestion();
        Long recordId = param.getRecordId();
        Long teachingQuestionBankId = param.getTeachingQuestionBankId();

        if (studentId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "学生ID不能为空");
        }
        if (teachingQuestionId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "题目ID不能为空");
        }
        if (recordId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "答卷记录ID不能为空");
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

        // 校验 teachingQuestionId
        LambdaQueryWrapper<TeachingQuestion> questionQueryWrapper = new LambdaQueryWrapper<>();
        questionQueryWrapper.eq(TeachingQuestion::getId, teachingQuestionId);
        boolean isQuestionExists = teachingQuestionMapper.exists(questionQueryWrapper);
        if (!isQuestionExists) {
            throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + teachingQuestionId + " 的题目不存在！");
        }

        // 校验 recordId
        LambdaQueryWrapper<StudentExercisesRecord> recordQueryWrapper = new LambdaQueryWrapper<>();
        recordQueryWrapper.eq(StudentExercisesRecord::getId, recordId);
        boolean isRecordExists = studentExercisesRecordMapper.exists(recordQueryWrapper);
        if (!isRecordExists) {
            throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + recordId + " 的答卷记录不存在！");
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

    @Override
    public Void deleteStudentExercisesContentByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 批量查询
        List<StudentExercisesContent> found = studentExercisesContentMapper.selectBatchIds(idList);
        // 如果数量不一致，则说明有遗漏
        if (found.size() != idList.size()) {
            // 找出那些不存在的 ID
            List<Long> foundIds = found.stream()
                    .map(StudentExercisesContent::getId)
                    .collect(Collectors.toList());
            List<Long> missing = idList.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "未找到对应的学生做题内容 ID：" + missing
            );
        }
        this.removeByIds(idList);

        return null;
    }

    @Override
    public void updateStudentExercisesContent(StudentExercisesContent param) {
        // 校验要更新的记录本身是否存在
        Long studentExercisesContentId = param.getId();
        if (studentExercisesContentId == null) {
            throw new RuntimeException("更新失败，未提供学生做题内容ID！");
        }

        StudentExercisesContent oldstudentExercisesContent = this.getById(studentExercisesContentId);
        if (oldstudentExercisesContent == null) {
            throw new RuntimeException("ID为 " + studentExercisesContentId + " 的学生做题内容不存在，无法更新！");
        }

        Long studentId = param.getStudentId();
        if (studentId != null) {
            if (!studentId.equals(oldstudentExercisesContent.getStudentId())) {
                boolean isStudentExists = studentMapper.exists(
                        new LambdaQueryWrapper<Student>().eq(Student::getId, studentId)
                );
                if (!isStudentExists) {
                    throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + studentId + " 的学生不存在！");
                }
            }
        }

        Long teachingQuestionId = param.getTeachingQuestion();
        if (teachingQuestionId != null) {
            if (!teachingQuestionId.equals(oldstudentExercisesContent.getTeachingQuestion())) {
                boolean isQuestionExists = teachingQuestionMapper.exists(
                        new LambdaQueryWrapper<TeachingQuestion>().eq(TeachingQuestion::getId, teachingQuestionId)
                );
                if (!isQuestionExists) {
                    throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + teachingQuestionId + " 的题目不存在！");
                }
            }
        }

        Long recordId = param.getRecordId();
        if (recordId != null) {
            if (!recordId.equals(oldstudentExercisesContent.getRecordId())) {
                boolean isRecordExists = studentExercisesRecordMapper.exists(
                        new LambdaQueryWrapper<StudentExercisesRecord>().eq(StudentExercisesRecord::getId, recordId)
                );
                if (!isRecordExists) {
                    throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "ID为 " + recordId + " 的答卷记录不存在！");
                }
            }
        }

        Long teachingQuestionBankId = param.getTeachingQuestionBankId();
        if (teachingQuestionBankId != null) {
            if (!teachingQuestionBankId.equals(oldstudentExercisesContent.getTeachingQuestionBankId())) {
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
    public Page<StudentExercisesContent> selectStudentExercisesContentPage(StudentExercisesContentPageSearchParam param) {
        Page<StudentExercisesContent> page = new Page<>(param.getCurrent(), param.getSize());
        return studentExercisesContentMapper.selectStudentExercisesContentPage(page, param);
    }
}
