package com.upc.modular.questionbank.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.questionbank.controller.param.*;
import com.upc.modular.questionbank.entity.*;
import com.upc.modular.questionbank.mapper.QuestionsBanksListMapper;
import com.upc.modular.questionbank.mapper.StudentExercisesContentMapper;
import com.upc.modular.questionbank.mapper.StudentExercisesRecordMapper;
import com.upc.modular.questionbank.mapper.TeachingQuestionBankMapper;
import com.upc.modular.questionbank.service.IStudentExercisesRecordService;
import com.upc.modular.questionbank.service.ITeachingQuestionBankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.questionbank.controller.param.PendingReviewReturnVO;
import com.upc.modular.questionbank.controller.param.PendingReviewSearchParam;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.TextbookCatalogMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Slf4j
@Service
public class TeachingQuestionBankServiceImpl extends ServiceImpl<TeachingQuestionBankMapper, TeachingQuestionBank> implements ITeachingQuestionBankService {

    @Autowired
    private TeachingQuestionBankMapper teachingQuestionBankMapper;

    @Autowired
    private TextbookMapper textbookMapper;

    @Autowired
    private TextbookCatalogMapper textbookCatalogMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private StudentExercisesContentMapper studentExercisesContentMapper;

    @Autowired
    private StudentExercisesRecordMapper studentExercisesRecordMapper;

    @Autowired
    private IStudentExercisesRecordService studentExercisesRecordService;

    @Autowired
    private QuestionsBanksListMapper questionsBanksListMapper;
    @Autowired
    private SysUserMapper sysUserMapper;



    @Override
    public Void deleteQuestionBankByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 批量查询
        List<TeachingQuestionBank> found = teachingQuestionBankMapper.selectBatchIds(idList);
        // 如果数量不一致，则说明有遗漏
        if (found.size() != idList.size()) {
            // 找出那些不存在的 ID
            List<Long> foundIds = found.stream()
                    .map(TeachingQuestionBank::getId)
                    .collect(Collectors.toList());
            List<Long> missing = idList.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new BusinessException(
                    BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "未找到对应的题库 ID：" + missing
            );
        }
        this.removeByIds(idList);

        return null;
    }

    @Override
    public Page<TeachingQuestionBankPageReturnParam> selectQuestionBankPage(TeachingQuestionBankPageSearchParam param) {
        Long userId = UserUtils.get().getId();
        // --- 第一阶段：数据库查询 ---
        // 1. 创建分页对象，注意泛型是中间结果类型
        Page<TeachingQuestionBankPageMidReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        // 2. 调用Mapper获取包含 creatorId 和 questionCount 的分页数据
        Page<TeachingQuestionBankPageMidReturnParam> midResultPage = teachingQuestionBankMapper.selectQuestionBank(page, param, userId);
        // --- 第二阶段：Java内存中数据处理和转换 ---
        List<TeachingQuestionBankPageMidReturnParam> midRecords = midResultPage.getRecords();
        // 如果查询结果为空，直接返回一个空的最终分页对象，保留分页信息
        if (CollectionUtils.isEmpty(midRecords)) {
            return new Page<>(midResultPage.getCurrent(), midResultPage.getSize(), 0);
        }

        // 3. 提取所有不重复的创建者ID (creator)
        Set<Long> creatorIds = midRecords.stream()
                .map(TeachingQuestionBankPageMidReturnParam::getCreator)
                .filter(id -> id != null) // 过滤掉可能为null的id
                .collect(Collectors.toSet());
        // 4. 一次性查询所有相关的用户姓名（从SysTbuser表中获取nickname）
        Map<Long, String> creatorIdToNameMap;
        if (!creatorIds.isEmpty()) {
            // 从SysTbuser表中查询用户信息
            List<SysTbuser> users = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysTbuser>()
                            .in(SysTbuser::getId, creatorIds)
            );
            // 将查询结果转为 Map<ID, nickname> 以便快速查找
            creatorIdToNameMap = users.stream()
                    .collect(Collectors.toMap(SysTbuser::getId, SysTbuser::getNickname, (oldValue, newValue) -> oldValue));
        } else {
            creatorIdToNameMap = Collections.emptyMap();
        }
        // 5. 将中间结果(MidReturnParam)转换为最终结果(ReturnParam)
        List<TeachingQuestionBankPageReturnParam> finalRecords = midRecords.stream().map(midParam -> {
            TeachingQuestionBankPageReturnParam finalParam = new TeachingQuestionBankPageReturnParam();
            // 复制所有同名属性
            BeanUtils.copyProperties(midParam, finalParam);
            // 单独处理 creatorName 字段，从ID转换为nickname
            String creatorName = creatorIdToNameMap.getOrDefault(midParam.getCreator(), "未知用户"); // 如果找不到，给个默认值
            finalParam.setCreatorName(creatorName);
            // 添加是否为当前用户创建的标识
            finalParam.setIsCreatedByCurrentUser(midParam.getCreator() != null && midParam.getCreator().equals(userId));
            return finalParam;
        }).collect(Collectors.toList());
        // 6. 构建并返回最终的分页对象
        Page<TeachingQuestionBankPageReturnParam> finalPage = new Page<>(
                midResultPage.getCurrent(),
                midResultPage.getSize(),
                midResultPage.getTotal()
        );
        finalPage.setRecords(finalRecords);
        return finalPage;
    }
@Override
public Long inserQuestionBank(TeachingQuestionBank param) {
    Long textbookId = param.getTextbookId();
    Long textbookCatalogId = param.getTextbookCatalogId();

    // 教材ID和教材目录ID是外键，需要判断是否存在
    if (textbookId != null) {
        LambdaQueryWrapper<Textbook> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Textbook::getId, textbookId);
        boolean isTextbookExists = textbookMapper.exists(queryWrapper1);
        if (!isTextbookExists) {
            throw new RuntimeException("ID为 " + textbookId + " 的教材不存在！");
        }
    }

    if (textbookCatalogId != null) {
        LambdaQueryWrapper<TextbookCatalog> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(TextbookCatalog::getId, textbookCatalogId);
        boolean isTextbookCatalogExists = textbookCatalogMapper.exists(queryWrapper2);
        if (!isTextbookCatalogExists) {
            throw new RuntimeException("ID为 " + textbookCatalogId + " 的教材目录不存在！");
        }
    }

    this.save(param);
    return param.getId();
}


    @Override
    public void updateQuestionBank(TeachingQuestionBank teachingQuestionbank) {
        // 1. 校验要更新的记录本身是否存在
        Long questionBankId = teachingQuestionbank.getId();
        if (questionBankId == null) {
            throw new RuntimeException("更新失败，未提供题库ID！");
        }
        if (teachingQuestionbank.getTextbookCatalogId() == null && teachingQuestionbank.getTextbookCatalogUuId() != null && !teachingQuestionbank.getTextbookCatalogUuId().trim().isEmpty()) {

            // 根据UUID查询 textbook_catalog 表
            LambdaQueryWrapper<TextbookCatalog> wrapper = new LambdaQueryWrapper<TextbookCatalog>()
                    .eq(TextbookCatalog::getCatalogUuid, teachingQuestionbank.getTextbookCatalogUuId())
                    .select(TextbookCatalog::getId); // 性能优化：只查询ID字段

            TextbookCatalog catalog = textbookCatalogMapper.selectOne(wrapper);

            if (catalog == null) {
                // 如果找不到，直接抛出异常，中断后续操作
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "提供的教材目录UUID无效: " + teachingQuestionbank.getTextbookCatalogUuId());
            }

            // 将查询到的ID回填到要更新的对象中
            teachingQuestionbank.setTextbookCatalogId(catalog.getId());
        }
        TeachingQuestionBank oldQuestionBank = this.getById(questionBankId);
        if (oldQuestionBank == null) {
            throw new RuntimeException("ID为 " + questionBankId + " 的题库不存在，无法更新！");
        }

        // 2. 校验外键（教材ID）是否存在
        Long textbookId = teachingQuestionbank.getTextbookId();
        if (ObjectUtils.isNotEmpty(textbookId) && ObjectUtils.isNotNull(textbookId)) {
            if (!textbookId.equals(oldQuestionBank.getTextbookId())) {
                boolean isTextbookExists = textbookMapper.exists(
                        new LambdaQueryWrapper<Textbook>().eq(Textbook::getId, textbookId)
                );
                if (!isTextbookExists) {
                    throw new RuntimeException("ID为 " + textbookId + " 的教材不存在！");
                }
            }
        }

        // 3. 校验外键（教材目录ID）是否存在
        Long textbookCatalogId = teachingQuestionbank.getTextbookCatalogId();
        if (ObjectUtils.isNotEmpty(textbookCatalogId) && ObjectUtils.isNotNull(textbookCatalogId)) {
            if (!textbookCatalogId.equals(oldQuestionBank.getTextbookCatalogId())) {
                boolean isTextbookCatalogExists = textbookCatalogMapper.exists(
                        new LambdaQueryWrapper<TextbookCatalog>().eq(TextbookCatalog::getId, textbookCatalogId)
                );
                if (!isTextbookCatalogExists) {
                    throw new RuntimeException("ID为 " + textbookCatalogId + " 的教材目录不存在！");
                }
            }
        }

        // 4. 所有校验通过，执行更新操作
        // updateById 会根据 teachingQuestionbank 对象的ID去更新其他非空字段
        this.updateById(teachingQuestionbank);
    }
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务保持不变
    public List<Long> updateQuestionBankBatch(List<TeachingQuestionBank> teachingQuestionBanks) {
        if (CollectionUtils.isEmpty(teachingQuestionBanks)) {
            return Collections.emptyList(); // 列表为空，直接返回空列表
        }
        // 1. 收集所有需要通过 UUID 来补全 ID 的记录的 UUID
        List<String> uuidsToResolve = teachingQuestionBanks.stream()
                .filter(bank -> bank.getTextbookCatalogId() == null && bank.getTextbookCatalogUuId() != null && !bank.getTextbookCatalogUuId().trim().isEmpty())
                .map(TeachingQuestionBank::getTextbookCatalogUuId)
                .distinct()
                .collect(Collectors.toList());

        // 2. 如果存在需要解析的 UUID，则一次性批量查询
        if (!CollectionUtils.isEmpty(uuidsToResolve)) {
            // 查询 textbook_catalog 表，获取 UUID 与 ID 的对应关系
            List<TextbookCatalog> catalogs = textbookCatalogMapper.selectList(
                    new LambdaQueryWrapper<TextbookCatalog>()
                            .in(TextbookCatalog::getCatalogUuid, uuidsToResolve)
                            .select(TextbookCatalog::getId, TextbookCatalog::getCatalogUuid) // 性能优化
            );

            // 创建一个 UUID -> ID 的快速查找Map
            Map<String, Long> uuidToIdMap = catalogs.stream()
                    .collect(Collectors.toMap(TextbookCatalog::getCatalogUuid, TextbookCatalog::getId));
            // 3. 校验所有传入的 UUID 是否都有效
            if (uuidToIdMap.size() < uuidsToResolve.size()) {
                String notFoundUuids = uuidsToResolve.stream()
                        .filter(uuid -> !uuidToIdMap.containsKey(uuid))
                        .collect(Collectors.joining(", "));
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "更新失败，以下指定的教材目录UUID不存在: " + notFoundUuids);
            }

            // 4. 遍历输入列表，回填缺失的 textbookCatalogId
            for (TeachingQuestionBank bank : teachingQuestionBanks) {
                if (bank.getTextbookCatalogId() == null && bank.getTextbookCatalogUuId() != null && !bank.getTextbookCatalogUuId().trim().isEmpty()) {
                    bank.setTextbookCatalogId(uuidToIdMap.get(bank.getTextbookCatalogUuId()));
                }
            }
        }
        // 1. 提取所有待更新记录的ID
        List<Long> questionBankIds = teachingQuestionBanks.stream()
                .map(TeachingQuestionBank::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (questionBankIds.size() != teachingQuestionBanks.size()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "更新失败，部分题库记录未提供ID！");
        }
        // 2. 校验所有待更新的题库ID本身是否存在
        List<TeachingQuestionBank> oldQuestionBanks = this.listByIds(questionBankIds);
        if (oldQuestionBanks.size() != questionBankIds.size()) {
            Set<Long> foundIds = oldQuestionBanks.stream().map(TeachingQuestionBank::getId).collect(Collectors.toSet());
            List<Long> missingIds = questionBankIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                    "ID为 " + missingIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + " 的题库不存在，无法更新！");

        }
        // 3. 准备校验有变动的关联ID (教材ID和目录ID)
        Map<Long, TeachingQuestionBank> oldBankMap = oldQuestionBanks.stream()
                .collect(Collectors.toMap(TeachingQuestionBank::getId, bank -> bank));

        Set<Long> textbookIdsToValidate = teachingQuestionBanks.stream()
                .filter(newBank -> {
                    TeachingQuestionBank oldBank = oldBankMap.get(newBank.getId());
                    return newBank.getTextbookId() != null && !newBank.getTextbookId().equals(oldBank.getTextbookId());
                })
                .map(TeachingQuestionBank::getTextbookId)
                .collect(Collectors.toSet());

        Set<Long> catalogIdsToValidate = teachingQuestionBanks.stream()
                .filter(newBank -> {
                    TeachingQuestionBank oldBank = oldBankMap.get(newBank.getId());
                    return newBank.getTextbookCatalogId() != null && !newBank.getTextbookCatalogId().equals(oldBank.getTextbookCatalogId());
                })
                .map(TeachingQuestionBank::getTextbookCatalogId)
                .collect(Collectors.toSet());

        // 4. 【核心修改】执行精确校验
        if (!CollectionUtils.isEmpty(textbookIdsToValidate)) {
            // 查询数据库中实际存在的ID
            List<Long> foundTextbookIds = textbookMapper.selectList(new LambdaQueryWrapper<Textbook>()
                            .select(Textbook::getId) // 仅查询ID字段，提升效率
                            .in(Textbook::getId, textbookIdsToValidate))
                    .stream()
                    .map(Textbook::getId)
                    .collect(Collectors.toList());

            // 如果查到的ID数量少于需要校验的ID数量，说明有ID不存在
            if (foundTextbookIds.size() < textbookIdsToValidate.size()) {
                // 找出不存在的ID (差集)
                Set<Long> nonExistentIds = new HashSet<>(textbookIdsToValidate);
                nonExistentIds.removeAll(foundTextbookIds);
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "更新失败，以下指定的教材ID不存在: " + nonExistentIds.stream().map(String::valueOf).collect(Collectors.joining(",")));

            }
        }
        // 校验教材目录ID
        if (!CollectionUtils.isEmpty(catalogIdsToValidate)) {
            // 查询数据库中实际存在的ID
            List<Long> foundCatalogIds = textbookCatalogMapper.selectList(new LambdaQueryWrapper<TextbookCatalog>()
                            .select(TextbookCatalog::getId)
                            .in(TextbookCatalog::getId, catalogIdsToValidate))
                    .stream()
                    .map(TextbookCatalog::getId)
                    .collect(Collectors.toList());

            if (foundCatalogIds.size() < catalogIdsToValidate.size()) {
                // 找出不存在的ID (差集)
                Set<Long> nonExistentIds = new HashSet<>(catalogIdsToValidate);
                nonExistentIds.removeAll(foundCatalogIds);
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "更新失败，以下指定的教材目录ID不存在: " + nonExistentIds.stream().map(String::valueOf).collect(Collectors.joining(",")));

            }
        }
        // 5. 在更新前，先清除本次操作涉及章节下所有题库的旧绑定关系
        Set<Long> catalogIdsToClear = teachingQuestionBanks.stream()
                .map(TeachingQuestionBank::getTextbookCatalogId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 5.2. 如果存在需要操作的目录，则执行解绑
        if (!CollectionUtils.isEmpty(catalogIdsToClear)) {
            LambdaUpdateWrapper<TeachingQuestionBank> clearBindingWrapper = new LambdaUpdateWrapper<>();
            clearBindingWrapper
                    .in(TeachingQuestionBank::getTextbookCatalogId, catalogIdsToClear)
                    .set(TeachingQuestionBank::getTextbookCatalogId, null)
                    .set(TeachingQuestionBank::getTextbookId, null);

            // 执行批量解绑操作
            this.update(clearBindingWrapper);
        }
        // 5. 所有校验通过，执行批量更新
        this.updateBatchById(teachingQuestionBanks);
        // 6. 【核心修改】返回成功更新的题库ID列表
        return questionBankIds;
    }

    public TeachingQuestionBankWithCreatorReturnParam getQuestionBankWithCreator(Long id) {
        TeachingQuestionBank questionBank = this.getById(id);
        if (questionBank == null) {
            return null;
        }

        TeachingQuestionBankWithCreatorReturnParam result = new TeachingQuestionBankWithCreatorReturnParam();
        // 复制题库基本信息
        result.setId(questionBank.getId());
        result.setName(questionBank.getName());
        result.setDescription(questionBank.getDescription());
        result.setStatus(questionBank.getStatus());
        result.setTextbookId(questionBank.getTextbookId());
        result.setTextbookCatalogId(questionBank.getTextbookCatalogId());
        result.setIsLimitAttempts(questionBank.getIsLimitAttempts());
        result.setMaxAttempts(questionBank.getMaxAttempts());
        result.setScorePolicy(questionBank.getScorePolicy());
        result.setCreator(questionBank.getCreator());
        result.setAddDatetime(questionBank.getAddDatetime());
        result.setOperator(questionBank.getOperator());
        result.setOperationDatetime(questionBank.getOperationDatetime());

        // 获取创建人姓名
        if (questionBank.getCreator() != null) {
            SysTbuser creator = sysUserMapper.selectById(questionBank.getCreator());
            if (creator != null) {
                result.setCreatorName(creator.getNickname());
            }
        }

        return result;
    }

    @Override
    public TeachingQuestionBankGetBankExerAttempAndStudentNumReturnParam getBankExerAttempAndStudentNum(TeachingQuestionBankGetBankExerAttempAndStudentNumSearchParam param) {
        // 1. 获取当前登录的学生ID，这种方式比前端传递更安全
        Long userId = UserUtils.get().getId();

        LambdaQueryWrapper<Student> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Student::getUserId, userId);
        Long studentId = studentMapper.selectOne(queryWrapper1).getId();

        // 2. 查询题库信息以获取最大答题次数限制
        TeachingQuestionBank bank = teachingQuestionBankMapper.selectById(param.getBankId());
        if (bank == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "题库不存在");
        }

        TeachingQuestionBankGetBankExerAttempAndStudentNumReturnParam result = new TeachingQuestionBankGetBankExerAttempAndStudentNumReturnParam();

        // 3. 根据 is_limit_attempts 字段判断次数是否受限
        // is_limit_attempts: 0-不限制, 1-限制
        if (Integer.valueOf(1).equals(bank.getIsLimitAttempts())) {
            result.setMaxAttempts(bank.getMaxAttempts());
        } else {
            // 不限制时，返回 -1 作为特殊标识
            result.setMaxAttempts(-1);
        }

        // 4. 查询学生对该题库的最高答题次数记录
        LambdaQueryWrapper<StudentExercisesRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StudentExercisesRecord::getTeachingQuestionBankId, param.getBankId())
                .eq(StudentExercisesRecord::getStudentId, studentId)
                .orderByDesc(StudentExercisesRecord::getExerciseNum) // 按答题次数降序排序
                .last("LIMIT 1"); // 只取最大的一条记录，提高查询效率

        StudentExercisesRecord lastRecord = studentExercisesRecordMapper.selectOne(queryWrapper);

        // 5. 设置学生已答题次数
        // 如果 lastRecord 不为 null，则取其 exerciseNum；否则说明学生从未答过，次数为 0
        int studentAttemptedNum = Optional.ofNullable(lastRecord)
                .map(StudentExercisesRecord::getExerciseNum)
                .orElse(0);
        result.setStudentAttemptedNum(studentAttemptedNum);

        return result;
    }

    @Override
    public List<QuestionBankWithStatusVO> getQuestionBanksWithStatusForTextbook(QuestionBankWithStatusSearchParam param) {
        List<QuestionBankWithStatusVO> resultList = teachingQuestionBankMapper.selectQuestionBanksWithPendingStatus(
                param.getTextbookId(),
                param.getTextbookCatalogId(),
                param.getTeachingQuestionBankName()
        );

        if (resultList != null && !resultList.isEmpty()) {
            for (QuestionBankWithStatusVO vo : resultList) {
                if (StringUtils.hasText(vo.getCatalogName())) {
                    String rawHtml = vo.getCatalogName();
                    String plainText = Jsoup.parse(rawHtml).text();
                    vo.setCatalogName(plainText);
                }
            }
        }
        return resultList;
    }

    @Override
    public Page<GradingSituationReturnVO> getGradingSituationPage(GradingSituationSearchParam param) {
        // 调试日志：检查传入的参数值
        log.info("开始分页查询答题情况，参数 bankId: {}", param.getBankId());
        Page<GradingSituationReturnVO> page = new Page<>(param.getCurrent(), param.getSize());
        return teachingQuestionBankMapper.selectGradingSituationPage(page, param);
    }

    @Override
    public List<StudentAnswerDetailVO> getStudentAnswerDetails(Long recordId) {
        List<StudentAnswerDetailVO> details = teachingQuestionBankMapper.selectStudentAnswerDetailsByRecordId(recordId);

        for (StudentAnswerDetailVO detail : details) {
            if (detail.getStudentScore() != null && detail.getMaxScore() != null) {
                // 如果得分等于满分，则认为是正确的
                detail.setIsCorrect(detail.getStudentScore().equals(detail.getMaxScore()));
            } else if (detail.getQuestionType() == 5) { // 主观题
                detail.setIsCorrect(null); // 主观题没有对错之分，设为null
            } else {
                detail.setIsCorrect(false); // 其他情况（如0分）视为错误
            }
        }

        return details;
    }

//    @Override
//    public Page<PendingReviewReturnVO> selectPendingReviewPage(PendingReviewSearchParam param) {
//        Page<PendingReviewReturnVO> page = new Page<>(param.getCurrent(), param.getSize());
//        Long currentTeacherUserId = UserUtils.get().getId();

    /// /        Long currentTeacherUserId = 16L;
//        Long teacherId = teacherMapper.selectOne(
//                new LambdaQueryWrapper<Teacher>()
//                        .eq(Teacher::getUserId,currentTeacherUserId)
//        ).getId();
//        param.setTeacherId(teacherId);
//        return teachingQuestionBankMapper.selectPendingReviewPage(page, param);
//    }
    @Override
    public List<PendingReviewQuestionVO> getPendingReviewByQuestion(PendingReviewSearchParam param) {
//        // 1. 设置权限信息
//        Long currentUserId = UserUtils.get().getId();
//        Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, currentUserId));
//        if (teacher == null) return new ArrayList<>(); // 不是老师，返回空
//        param.setTeacherId(teacher.getId());

        // 2. 从数据库获取扁平化的原始数据列表
        List<PendingReviewRawDataVO> rawDataList = baseMapper.selectPendingReviewRawDataForBank(param);
        if (rawDataList.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 在Java内存中进行数据聚合
        // 使用 Stream API 的 groupingBy 将列表按 questionId 分组
        Map<Long, List<PendingReviewRawDataVO>> groupedByQuestion = rawDataList.stream()
                .collect(Collectors.groupingBy(PendingReviewRawDataVO::getQuestionId));

        // 4. 遍历分组，组装成最终的嵌套结构
        List<PendingReviewQuestionVO> finalResult = new ArrayList<>();

        for (Map.Entry<Long, List<PendingReviewRawDataVO>> entry : groupedByQuestion.entrySet()) {
            PendingReviewQuestionVO questionVO = new PendingReviewQuestionVO();

            List<PendingReviewRawDataVO> answersForThisQuestion = entry.getValue();
            PendingReviewRawDataVO firstRecord = answersForThisQuestion.get(0);

            // 填充题目信息
            questionVO.setQuestionId(firstRecord.getQuestionId());
            questionVO.setQuestionContent(firstRecord.getQuestionContent());
            questionVO.setCorrectAnswer(firstRecord.getCorrectAnswer());
            questionVO.setAnswerAnalysis(firstRecord.getAnswerAnalysis());
            questionVO.setMaxScore(firstRecord.getMaxScore());

            // 遍历该题下的所有学生回答，组装成内层列表
            List<StudentAnswerForReviewVO> studentAnswers = answersForThisQuestion.stream().map(rawData -> {
                StudentAnswerForReviewVO answerVO = new StudentAnswerForReviewVO();
                answerVO.setContentId(rawData.getContentId());
                answerVO.setRecordId(rawData.getRecordId());
                answerVO.setStudentName(rawData.getStudentName());
                answerVO.setStudentAnswer(rawData.getStudentAnswer());
                return answerVO;
            }).collect(Collectors.toList());

            questionVO.setStudentAnswers(studentAnswers);

            finalResult.add(questionVO);
        }

        return finalResult;
    }


    @Override
    @Transactional
    public void gradeSubjectiveQuestion(GradeSubjectiveRequest request) {
        // --- 1. 基础校验 ---
        StudentExercisesContent content = studentExercisesContentMapper.selectById(request.getContentId());
        if (content == null) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ",答题记录不存在！!");
        }
        if (content.getScore() != null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, ",该题目已被评分，请勿重复操作！");
        }
        if (request.getScore() == null || request.getScore() < 0) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, ",评分分数不能为空或小于0！");
        }

        // 分数超限校验
        // 从答题明细中获取 questionId 和 bankId
        Long questionId = content.getTeachingQuestion(); // 假设已改为 questionId
        Long bankId = content.getTeachingQuestionBankId();

        // 查询这道题在这个题库里的满分值
        LambdaQueryWrapper<QuestionsBanksList> qw = new LambdaQueryWrapper<>();
        qw.eq(QuestionsBanksList::getBankId, bankId)
                .eq(QuestionsBanksList::getQuestionId, questionId);

        QuestionsBanksList questionBankInfo = questionsBanksListMapper.selectOne(qw);

        if (questionBankInfo != null && questionBankInfo.getScore() != null) {
            double maxScore = questionBankInfo.getScore();
            if (request.getScore() > maxScore) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, ",评分失败：分数不能超过该题目的满分!");
            }
        } else {
            // 如果在关联表中找不到这道题的分数信息，可以根据业务决定是报错还是忽略校验,这里我们选择报错，因为数据不完整
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, ",无法获取该题目的满分值信息，请检查题库配置。");
        }

        // 2. 更新单题分数
        content.setScore(request.getScore());
        studentExercisesContentMapper.updateById(content);

        // 3. 检查整份答卷是否已全部批改完毕
        Long recordId = content.getRecordId();
        long pendingCount = studentExercisesContentMapper.selectCount(new LambdaQueryWrapper<StudentExercisesContent>()
                .eq(StudentExercisesContent::getRecordId, recordId)
                .isNull(StudentExercisesContent::getScore));

        if (pendingCount == 0) {
            // 所有题目都已评分，计算总分并更新答卷状态
            // 重新获取所有明细来计算总分
            double totalScore = studentExercisesContentMapper.selectList(
                            new LambdaQueryWrapper<StudentExercisesContent>()
                                    .eq(StudentExercisesContent::getRecordId, recordId)
                    ).stream()
                    .filter(c -> c.getScore() != null)
                    .mapToDouble(StudentExercisesContent::getScore)
                    .sum();

            StudentExercisesRecord recordToUpdate = new StudentExercisesRecord();
            recordToUpdate.setId(recordId);
            recordToUpdate.setStatus(2); // 2-已完成
            recordToUpdate.setScore(totalScore);
            studentExercisesRecordMapper.updateById(recordToUpdate);

            // 调用 IStudentExercisesRecordService 的方法 ---
            studentExercisesRecordService.calculateAndUpdateFinalGrade(recordId);
        }
    }

    //根据题库ID查询该题库下所有题目信息
    @Override
    public List<QuestionsBanksListVO> getQuestionsWithTypeNameByBankId(Long bankId) {
        return questionsBanksListMapper.selectQuestionsWithTypeNameByBankId(bankId);
    }
}
