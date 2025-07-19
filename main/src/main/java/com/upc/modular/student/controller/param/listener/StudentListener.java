package com.upc.modular.student.controller.param.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.IUserRoleListService;
import com.upc.modular.group.entity.UserClassList; // Import UserClassList
import com.upc.modular.group.service.IUserClassListService; // Import IUserClassListService
import com.upc.modular.student.controller.param.dto.StudentImportDto;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.student.service.impl.StudentServiceImpl; // Import the specific implementation to access saveBatchUsers
import com.upc.utils.AgeQuantifyUtils;
import com.upc.utils.MD5Utils;
import com.upc.utils.TypeConversionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.upc.modular.student.controller.param.StudentImportErrorDto;// 确保这里导入的是您提供的 StudentImportErrorDto 类

@Slf4j
public class StudentListener extends AnalysisEventListener<StudentImportDto> {
private static final int BATCH_COUNT = 1000;

private final Map<String, Student> existStudentMap;
private final StudentServiceImpl studentService;
private final ISysUserService sysUserService;
private final IUserRoleListService userRoleListService;
private final IUserClassListService userClassListService;
private final Map<String, Long> classMap;
private final Map<String, Long> institutionMap;
private final Long studentRoleId;


        List<SysTbuser> userInsertList = new ArrayList<>(BATCH_COUNT);
        List<Student> studentInsertList = new ArrayList<>(BATCH_COUNT);
        List<UserRoleList> userRoleInsertList = new ArrayList<>(BATCH_COUNT);
        List<UserClassList> userClassInsertList = new ArrayList<>(BATCH_COUNT);

        List<Student> studentUpdateList = new ArrayList<>(BATCH_COUNT);

@Getter
private long insertTotal;
@Getter
// *** 关键修改：将 errorList 的类型改为 List<StudentImportErrorDto> ***
private final List<StudentImportErrorDto> errorList = new ArrayList<>(); // 错误信息列表
@Getter
private long updateTotal;


public StudentListener(StudentServiceImpl studentService,
        ISysUserService sysUserService,
        IUserRoleListService userRoleListService,
        IUserClassListService userClassListService,
        Map<String, Student> existStudentMap,
        Map<String, Long> classMap,
        Map<String, Long> institutionMap,
        Long studentRoleId) {
        this.studentService = studentService;
        this.sysUserService = sysUserService;
        this.userRoleListService = userRoleListService;
        this.userClassListService = userClassListService;
        this.existStudentMap = existStudentMap;
        this.classMap = classMap;
        this.institutionMap = institutionMap;
        this.studentRoleId = studentRoleId;
        }

@Override
public void invoke(StudentImportDto dto, AnalysisContext context) {
        try {
        int rowIndex = context.readRowHolder().getRowIndex() + 1;

        String identityId = dto.getIdentityId();
        if (StringUtils.isBlank(identityId)) {
        // *** 关键修改：添加 StudentImportErrorDto 实例 ***
        errorList.add(new StudentImportErrorDto().setErrorReason("第" + rowIndex + "行：学号不能为空"));
        return;
        }
        if (StringUtils.isBlank(dto.getName())) {
        // *** 关键修改：添加 StudentImportErrorDto 实例 ***
        errorList.add(new StudentImportErrorDto().setErrorReason("第" + rowIndex + "行：姓名不能为空，学号: " + identityId));
        return;
        }

                String className = dto.getClassName();
                Long classId = null; // 默认为 null
                if (StringUtils.isNotBlank(className)) {
                        classId = classMap.get(className);
                        if (classId == null) {
                                // (可选) 如果你仍然想在最终的报告中看到这个警告，可以保留这行日志，但不要 return
                                log.warn("第{}行：学号[{}]的班级名称[{}]在班级表中不存在，班级ID将设置为空。", rowIndex, identityId, className);
                        }
        }

        Long institutionId = null;
        if (StringUtils.isNotBlank(className)) {
        institutionId = institutionMap.get(className);
        if (institutionId == null) {
        // *** 关键修改：添加 StudentImportErrorDto 实例 ***
        errorList.add(new StudentImportErrorDto().setErrorReason("第" + rowIndex + "行：学号[" + identityId + "]对应的班级名称[" + className + "]在机构表中不存在，用户的机构ID将设置为NULL。"));
        // institutionId 保持为 null
        }
        }

        Student student = new Student();
        BeanUtils.copyProperties(dto, student);
        student.setClassId(classId);

        String idcard = dto.getIdcard();
        if (StringUtils.isNotBlank(idcard)) {
        if (idcard.length() >= 14) {
        student.setBirthday(AgeQuantifyUtils.getBirthDateFromIdNumber(idcard));
        student.setGender(TypeConversionUtils.sexToString(AgeQuantifyUtils.getGenderFromIdNumber(idcard)));
        } else {
        log.warn("第{}行：学号[{}]身份证号[{}]格式不正确，无法自动计算生日和性别", rowIndex, identityId, idcard);
        }
        }

        Student existStudent = existStudentMap.get(identityId);
        if (existStudent != null) {
        student.setId(existStudent.getId());
        student.setUserId(existStudent.getUserId());
        studentUpdateList.add(student);
        updateTotal++;
        } else {
        studentInsertList.add(student);

        SysTbuser user = new SysTbuser()
        .setUsername(identityId)
        .setPassword(MD5Utils.md5(identityId))
        .setUserType(1)
        .setStatus(1)
        .setInstitutionId(institutionId)
        .setAddDatetime(LocalDateTime.now());
        userInsertList.add(user);

        insertTotal++;
        }

        if (studentInsertList.size() >= BATCH_COUNT || studentUpdateList.size() >= BATCH_COUNT) {
        saveData();
        clearLists();
        }
        } catch (Exception e) {
        // *** 关键修改：添加 StudentImportErrorDto 实例 ***
        errorList.add(new StudentImportErrorDto().setErrorReason("第" + (context.readRowHolder().getRowIndex() + 1) + "行：处理异常，学号[" + dto.getIdentityId() + "] - " + e.getMessage()));
        log.error("导入学生异常：学号[{}]", dto.getIdentityId(), e);
        }
        }

@Override
public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("导入完成，新增：{}，更新：{}，失败：{}", insertTotal, updateTotal, errorList.size());
        }

private void saveData() {
        if (CollectionUtils.isNotEmpty(userInsertList)) {
        studentService.saveBatchUsers(userInsertList);

        for (int i = 0; i < userInsertList.size(); i++) {
        SysTbuser user = userInsertList.get(i);
        Student student = studentInsertList.get(i);

        student.setUserId(user.getId());
       if (this.studentRoleId != null) {
               UserRoleList userRole = new UserRoleList()
                       .setUserId(user.getId())
                       .setRoleId(this.studentRoleId)
                       .setAddDatetime(LocalDateTime.now());
               userRoleInsertList.add(userRole);
       }


                if (student.getClassId() != null) {
                        UserClassList userClass = new UserClassList()
                                .setUserId(user.getId())
                                .setClassId(student.getClassId())
                                .setType(1)
                                .setAddDatetime(LocalDateTime.now());
                        userClassInsertList.add(userClass);
                }
        }
        }

        if (CollectionUtils.isNotEmpty(studentInsertList)) {
        studentService.saveBatch(studentInsertList);
        }

        if (CollectionUtils.isNotEmpty(userRoleInsertList)) {
        userRoleListService.saveBatch(userRoleInsertList);
        }

        if (CollectionUtils.isNotEmpty(userClassInsertList)) {
        userClassListService.saveBatch(userClassInsertList);
        }

        if (CollectionUtils.isNotEmpty(studentUpdateList)) {
        studentService.updateBatchById(studentUpdateList);
        }
        }

private void clearLists() {
        studentInsertList.clear();
        userInsertList.clear();
        userRoleInsertList.clear();
        userClassInsertList.clear();
        studentUpdateList.clear();
        }
        }