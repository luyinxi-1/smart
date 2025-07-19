package com.upc.modular.teacher.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.mapper.SysRoleMapper;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.mapper.UserRoleListMapper;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.IUserRoleListService;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.mapper.InstitutionMapper;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.modular.questionbank.entity.StudentExercisesContent;
import com.upc.modular.questionbank.entity.StudentExercisesRecord;
import com.upc.modular.questionbank.mapper.StudentExercisesContentMapper;
import com.upc.modular.questionbank.mapper.StudentExercisesRecordMapper;
import com.upc.modular.questionbank.service.IStudentExercisesRecordService;
import com.upc.modular.teacher.dto.*;
import com.upc.modular.teacher.vo.GenerateUserResultVo;
import com.upc.modular.teacher.vo.ImportTeacherReturnVo;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.listener.TeacherListener;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teacher.service.ITeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import com.upc.utils.InstitutionUtil;
import com.upc.utils.MD5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements ITeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private IUserRoleListService userRoleListService;

    @Autowired
    private SysRoleMapper sysRoleMapper;
    
    @Autowired
    private StudentExercisesContentMapper studentExercisesContentMapper;

    @Autowired
    private StudentExercisesRecordMapper studentExercisesRecordMapper;

    @Autowired
    private IStudentExercisesRecordService studentExercisesRecordService;

    @Override
    @Transactional
    public Boolean insert(TeacherInsertDto teacher) {
        if (ObjectUtils.isEmpty(teacher.getIdentityId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教师工号不能为空");
        }
        MyLambdaQueryWrapper<Teacher> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Teacher::getIdentityId, teacher.getIdentityId());
        List<Teacher> teachers = teacherMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(teachers)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教师工号已存在");
        }
        SysTbuser user = new SysTbuser()
                .setUsername(teacher.getIdentityId())
                .setPassword(MD5Utils.sha256(teacher.getIdentityId()))
                .setUserType(2) // 教师
                .setInstitutionId(teacher.getInstitutionId())
                .setStatus(1); // 默认启用

        sysUserMapper.insert(user);
        Teacher newTeacher = new Teacher();
        BeanUtils.copyProperties(teacher, newTeacher);
        newTeacher.setUserId(user.getId());
        this.save(newTeacher);
        MyLambdaQueryWrapper<SysTbrole> sysTbroleMyLambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        sysTbroleMyLambdaQueryWrapper.eq(SysTbrole::getRoleName, "普通教师");
        List<SysTbrole> sysTbroles = sysRoleMapper.selectList(sysTbroleMyLambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(sysTbroles)) {
            Long id = sysTbroles.get(0).getId();
            UserRoleList userRoleList = new UserRoleList()
                    .setRoleId(id)
                    .setUserId(user.getId());
            userRoleListService.save(userRoleList);
        }
        return true;
    }

    @Override
    public Boolean batchDelete(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam) || ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        List<Long> idList = idParam.getIdList();
        // 查询教师记录
        List<Teacher> teachers = teacherMapper.selectBatchIds(idList);
        if (ObjectUtils.isEmpty(teachers)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到对应的教师记录");
        }

        // 提取对应的 userId 并删除用户
        List<Long> userIdList = teachers.stream()
                .map(Teacher::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ObjectUtils.isNotEmpty(userIdList)) {
            sysUserService.batchDelete(userIdList);
        }

        // 删除教师记录
        boolean b = this.removeBatchByIds(idList);
        return b;
    }

    @Override
    public Page<TeacherReturnVo> getPage(TeacherPageSearchDto param) {
        Page<TeacherReturnVo> page = new Page<>(param.getCurrent(), param.getSize());
        return teacherMapper.selectTeacherWithInstitution(page, param);
    }

    @Override
    public ImportTeacherReturnVo importTeacherData(MultipartFile file) {
        ExcelReader excelReader = null;
        List<Teacher> teachers = teacherMapper.selectList(null);
        ImportTeacherReturnVo importTeacherReturnParam = new ImportTeacherReturnVo();

        MyLambdaQueryWrapper<SysTbrole> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysTbrole::getRoleName, "普通教师");
        List<SysTbrole> sysTbroles = sysRoleMapper.selectList(lambdaQueryWrapper);
        Long id = 0L;
        if (ObjectUtils.isNotEmpty(sysTbroles)) {
             id = sysTbroles.get(0).getId();
        }
        List<Institution> institutions = institutionMapper.selectList(null);
        Map<String, Long> institutionMap = institutions.stream()
                .collect(Collectors.toMap(Institution::getInstitutionName, Institution::getId));

        try {
            TeacherListener teacherListener = new TeacherListener(this, teachers, sysUserMapper, institutionMap, userRoleListService, id);
            excelReader = EasyExcel.read(file.getInputStream(), TeacherImportDto.class, teacherListener).build();
            // 这两行用于执行读操作，不加不运行
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            excelReader.read(readSheet);
            importTeacherReturnParam.setInsertTotal(teacherListener.getInsertTotal());
            importTeacherReturnParam.setUpdateTotal(teacherListener.getUpdateTotal());
            importTeacherReturnParam.setErrorTotal(teacherListener.getErrorList().size());
            importTeacherReturnParam.setErrorDetails(teacherListener.getErrorList());
            return importTeacherReturnParam;
        } catch (IOException e) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "导入文件格式不合法");
        } finally {
            if (excelReader != null) {
                // 这里千万别忘记关闭，读的时候会创建临时文件，到时磁盘会崩的
                excelReader.finish();
            }
        }
    }

    @Override
    public SysTbuser getTeacherUser(TeacherReturnVo param) {
        if (ObjectUtils.isEmpty(param.getUserId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该教师未绑定用户");
        }
        MyLambdaQueryWrapper<SysTbuser> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysTbuser::getId, param.getUserId());
        return sysUserService.getOne(lambdaQueryWrapper);
    }


    @Override
    public List<TeacherReturnVo> getTeacherNoUser() {
        MyLambdaQueryWrapper<Teacher> queryWrapper = new MyLambdaQueryWrapper<>();
        queryWrapper.isNull(Teacher::getUserId);

        List<Teacher> teacherList = this.list(queryWrapper);

        // 映射为 TeacherReturnVo（假设结构一致，否则请手动赋值）
        return teacherList.stream().map(teacher -> {
            TeacherReturnVo vo = new TeacherReturnVo();
            BeanUtils.copyProperties(teacher, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public GenerateUserResultVo generateTeacherUsers(TeacherGenerateDto dto) {
        int successCount = 0;
        int failCount = 0;
        List<String> failedTeachers = new ArrayList<>();
        MyLambdaQueryWrapper<SysTbrole> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysTbrole::getRoleName, "普通教师");
        List<SysTbrole> sysTbroles = sysRoleMapper.selectList(lambdaQueryWrapper);
        Long roleId = 0L;
        if (ObjectUtils.isNotEmpty(sysTbroles)) {
            roleId = sysTbroles.get(0).getId();
        }

        for (TeacherReturnVo teacherVo : dto.getTeacher()) {
            try {
                if (teacherVo.getUserId() != null) {
                    continue; // 已绑定用户，跳过
                }

                String identityId = teacherVo.getIdentityId();
                if (identityId == null || identityId.trim().isEmpty()) {
                    failCount++;
                    failedTeachers.add(teacherVo.getName() + "(工号为空)");
                    continue;
                }

                // 构造用户
                SysTbuser user = new SysTbuser()
                        .setUsername(identityId)
                        .setPassword(MD5Utils.sha256(identityId))
                        .setUserType(2) // 教师
                        .setInstitutionId(dto.getInstitutionId())
                        .setStatus(1); // 默认启用

                sysUserMapper.insert(user);

                // 更新 teacher 的 user_id
                teacherVo.setUserId(user.getId());
                Teacher teacher = new Teacher();
                BeanUtils.copyProperties(teacherVo, teacher);
                teacherMapper.updateById(teacher);

                if (roleId != 0L) {
                    UserRoleList userOrRole = new UserRoleList();
                    userOrRole.setUserId(user.getId());
                    userOrRole.setRoleId(roleId);
                    userRoleListService.save(userOrRole);
                }

                successCount++;
            } catch (Exception e) {
                failCount++;
                failedTeachers.add(teacherVo.getName() + "(异常：" + e.getMessage() + ")");
            }
        }

        boolean allSuccess = failCount == 0;

        GenerateUserResultVo resultVo = new GenerateUserResultVo();
        resultVo.setAllSuccess(allSuccess);
        resultVo.setSuccessCount(successCount);
        resultVo.setFailCount(failCount);
        resultVo.setTotalProcessed(dto.getTeacher().size());
        resultVo.setFailedTeachers(failedTeachers);
        return resultVo;
    }

    @Override
    public Boolean updateTeacher(Teacher teacher) {
        if (ObjectUtils.isEmpty(teacher) || ObjectUtils.isEmpty(teacher.getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(teacher);
    }

    @Override
    public Boolean getTeacherIsInInstitution(GetTeacherIsInInstitutionParam param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getTeacherId()) || ObjectUtils.isEmpty(param.getInstitutionId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }

        // 获取所有子机构（含自身）
        List<Institution> institutions = institutionMapper.selectList(null);
        List<Long> allSubInstitutionIds = InstitutionUtil.getAllSubInstitutionIds(param.getInstitutionId(), institutions);

        // 查询教师对应的机构ID（通过teacher.user_id -> sys_tbuser.institution_id）
        Long institutionId = teacherMapper.getInstitutionIdByTeacherId(param.getTeacherId());

        if (institutionId == null) {
            return false;  // 教师没有绑定机构，直接false
        }

        return allSubInstitutionIds.contains(institutionId);
    }

    @Override
    @Transactional
    public void gradeSubjectiveQuestion(GradeSubjectiveRequest request) {
        // 1. 校验
        StudentExercisesContent content = studentExercisesContentMapper.selectById(request.getContentId());
        if (content == null) { throw new RuntimeException("答题记录不存在！"); }
        if (content.getScore() != null) { throw new RuntimeException("该题目已被评分！"); }

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

    @Override
    public Page<PendingReviewReturnVO> selectPendingReviewPage(PendingReviewSearchParam param) {
        Page<PendingReviewReturnVO> page = new Page<>(param.getCurrent(), param.getSize());
        return teacherMapper.selectPendingReviewPage(page, param);
    }


}
