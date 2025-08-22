package com.upc.modular.student.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.responseparam.R;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.mapper.SysLogMapper;
import com.upc.modular.auth.mapper.SysRoleMapper;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.IUserRoleListService;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.entity.UserClassList;
import com.upc.modular.group.mapper.GroupMapper;
import com.upc.modular.group.service.IUserClassListService;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.mapper.InstitutionMapper;
import com.upc.modular.student.controller.param.GetStudentIsInInstitutionParam;
import com.upc.modular.student.controller.param.StudentUserResultParam;
import com.upc.modular.student.controller.param.dto.StudentExportDto;
import com.upc.modular.student.controller.param.dto.StudentGenerateDto;
import com.upc.modular.student.controller.param.dto.StudentImportDto;
import com.upc.modular.student.controller.param.dto.StudentPageSearchDto;
import com.upc.modular.student.controller.param.listener.StudentListener;
import com.upc.modular.student.controller.param.vo.GenerateUserResultVoStudent;
import com.upc.modular.student.controller.param.vo.ImportStudentReturnVo;
import com.upc.modular.student.controller.param.vo.StudentExcelVo;
import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import com.upc.modular.student.converter.LocalDateTimeConverter;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teacher.vo.ImportTeacherReturnVo;
import com.upc.utils.AesCbcCompatUtil;
import com.upc.utils.InstitutionUtil;
import com.upc.utils.MD5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private SysLogMapper sysLogMapper;

    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private SysUserMapper sysUserMapper;



    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private IUserRoleListService userRoleListService;

    @Autowired
    private GroupMapper groupMapper; // Injected GroupMapper
    @Autowired
    private IUserClassListService userClassListService; // Injected IUserClassListService
    @Autowired
    private SysRoleMapper sysRoleMapper;


//    @Override
//    public void insertstudent(Student student) {
//        if (ObjectUtils.isEmpty(student.getIdentityId())) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户身份证号为空");
//        }
//        MyLambdaQueryWrapper<Student> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(Student::getIdentityId, student.getIdentityId());
//        List<Student> students = studentMapper.selectList(lambdaQueryWrapper);
//        if (ObjectUtils.isNotEmpty(students)) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户已存在");
//        }
//        this.save(student);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertstudent(Student student) {
        if (ObjectUtils.isEmpty(student.getIdentityId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户学号为空");
        }
        // 检查学生是否存在
        if (this.count(new QueryWrapper<Student>().eq("identity_id", student.getIdentityId())) > 0) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该学号的学生已存在");
        }
        // 检查用户账号是否存在
        if (sysUserService.count(new QueryWrapper<SysTbuser>().eq("username", student.getIdentityId())) > 0) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该学号的登录账号已存在");
        }

        // 1. 创建用户
        SysTbuser newUser = new SysTbuser();
        newUser.setUsername(student.getIdentityId());
        newUser.setNickname(student.getName());
        newUser.setPassword(AesCbcCompatUtil.encryptZeroBase64(student.getIdentityId()));
        newUser.setUserType(1); // 1代表学生
        newUser.setStatus(1);   // 默认启用
        // 根据最终确认，机构ID在这里暂时不设置
        newUser.setInstitutionId(null);
        sysUserService.save(newUser);

        // 2. 关联角色
        SysTbrole studentRole = sysRoleMapper.selectOne(new QueryWrapper<SysTbrole>().eq("role_code", "student"));
        if (studentRole == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "系统中未找到编码为'student'的学生角色，请先配置");
        }
        UserRoleList userRole = new UserRoleList();
        userRole.setUserId(newUser.getId());
        userRole.setRoleId(studentRole.getId());
        userRoleListService.save(userRole);

        // 3. 关联班级
        if (student.getClassId() != null) {
            UserClassList userClass = new UserClassList();
            userClass.setUserId(newUser.getId());
            userClass.setClassId(student.getClassId());
            userClass.setType(1); // 1代表学生
            userClassListService.save(userClass);
        }

        // 4. 保存学生信息
        student.setUserId(newUser.getId());
        this.save(student);
    }

    @Override
    public void deleteByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 查询学生记录
        List<Student> students = studentMapper.selectBatchIds(idList);
        if (ObjectUtils.isEmpty(students)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到对应的学生记录");
        }

        // 提取对应的 userId 并删除用户
        List<Long> userIdList = students.stream()
                .map(Student::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ObjectUtils.isNotEmpty(userIdList)) {
            sysUserService.batchDelete(userIdList);
        }
        this.removeBatchByIds(idList);
    }

    @Override
    public Page<StudentReturnVo> getPage(StudentPageSearchDto param) {
        Page<StudentReturnVo> page = new Page<>(param.getCurrent(), param.getSize());
        return studentMapper.selectStudentWithDetails(page, param);
    }

//    @Override
//    public ImportStudentReturnVo importStudentData(MultipartFile file) {
//        ImportStudentReturnVo importStudentReturnVo = new ImportStudentReturnVo();
//
//        // 1. 获取所有现有学生数据，并以学号为键构建 Map，用于快速查找学生是否存在（用于更新）
//        List<Student> existingStudents = studentMapper.selectList(null);
//        Map<String, Student> existStudentMap = existingStudents.stream()
//                .filter(s -> ObjectUtils.isNotEmpty(s.getIdentityId()))
//                .collect(Collectors.toMap(Student::getIdentityId, s -> s, (s1, s2) -> s1));
//
//        // 2. 获取“学生”角色的ID，后续创建用户时需要关联此角色
//        Long studentRoleId = getStudentRoleId();
//
//
//        // 3. 获取所有班级数据，并以班级名称为键构建 Map，用于将 Excel 中的班级名称转换为班级ID
//        List<Group> groups = groupMapper.selectList(null);
//        Map<String, Long> classMap = groups.stream()
//                .collect(Collectors.toMap(Group::getName, Group::getId, (g1, g2) -> g1));
//
//        // 4. 获取所有机构数据，并以机构名称为键构建 Map，用于将 Excel 中的班级名称作为机构名称去查找机构ID
//        List<Institution> institutions = institutionMapper.selectList(null);
//        Map<String, Long> institutionMap = institutions.stream()
//                .collect(Collectors.toMap(Institution::getInstitutionName, Institution::getId, (i1, i2) -> i1));
//// 创建 StudentListener 实例
//        StudentListener studentListener = new StudentListener(this, sysUserService, userRoleListService, userClassListService,
//                existStudentMap, classMap, institutionMap, studentRoleId);
//
//// 使用 EasyExcel 的链式调用来读取 Excel 文件。
//// EasyExcel.read() 方法会返回一个 ReadWorkbookBuilder，通过链式调用 .sheet() 和 .doRead() 来完成整个读取过程。
//// 这里的 try-with-resources 是为了确保文件输入流被正确关闭。
//        try { //
//            EasyExcel.read(file.getInputStream(), StudentImportDto.class, studentListener) //
//                    .sheet(0) // 读取第一个工作表（索引为0）
//                    .doRead(); // 执行读取操作
//
//            // 获取监听器中的结果。
//            // 因为 EasyExcel.read()...doRead() 已经完成了所有操作，
//            // studentListener 实例中已经包含了导入的统计结果和错误列表。
//            importStudentReturnVo.setErrorTotal(studentListener.getErrorList().size()); // 将失败条数赋值给 errorTotal
//            importStudentReturnVo.setInsertTotal(studentListener.getInsertTotal()); // 将新增数量赋值给 insertTotal
//            importStudentReturnVo.setUpdateTotal(studentListener.getUpdateTotal()); // 将更新数量赋值给 updateTotal
//            importStudentReturnVo.setErrorDetails(studentListener.getErrorList()); // 将出错详细信息赋值给 errorDetails
//
//        } catch (IOException e) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "文件读取失败: " + e.getMessage());
//        }
//
//        return importStudentReturnVo;
//    }

// StudentServiceImpl.java 中的 importStudentData 方法

// 文件位置: StudentServiceImpl.java

    @Override
    public ImportStudentReturnVo importStudentData(MultipartFile file) {
        // 1. 预加载数据
        Map<String, Student> existStudentMap = this.list().stream()
                .filter(s -> ObjectUtils.isNotEmpty(s.getIdentityId()))
                .collect(Collectors.toMap(Student::getIdentityId, s -> s, (s1, s2) -> s1));

        Map<String, SysTbuser> existUserMap = sysUserService.list().stream()
                .filter(u -> ObjectUtils.isNotEmpty(u.getUsername()))
                .collect(Collectors.toMap(SysTbuser::getUsername, u -> u, (u1, u2) -> u1));

        Map<String, Long> classMap = groupMapper.selectList(null).stream()
                .collect(Collectors.toMap(Group::getName, Group::getId, (g1, g2) -> g1));

        // ---------- ▼▼▼【这里是补充回来的关键代码】▼▼▼ ----------
        List<Institution> institutions = institutionMapper.selectList(null);
        Map<String, Long> institutionMap = institutions.stream()
                .collect(Collectors.toMap(Institution::getInstitutionName, Institution::getId, (i1, i2) -> i1));
        // ---------- ▲▲▲【补充代码结束】▲▲▲ ----------

        // 2. 获取学生角色ID
        SysTbrole studentRole = sysRoleMapper.selectOne(new QueryWrapper<SysTbrole>().eq("role_code", "student"));
        if (studentRole == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "系统中未找到编码为'student'的学生角色，请先配置");
        }
        Long studentRoleId = studentRole.getId();

        // 3. 创建 Listener 实例，现在所有参数都已定义
        StudentListener studentListener = new StudentListener(
                this,
                sysUserService,
                userRoleListService,
                userClassListService,
                existStudentMap,
                classMap,
                institutionMap,  //<-- 现在 institutionMap 变量存在了，不再报错
                studentRoleId
        );

        // 4. 读取Excel
        try {
            EasyExcel.read(file.getInputStream(), StudentImportDto.class, studentListener)
                    .sheet(0)
                    .doRead();
        } catch (IOException e) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "文件读取失败: " + e.getMessage());
        }

        // 5. 封装返回结果
        ImportStudentReturnVo returnVo = new ImportStudentReturnVo();
        returnVo.setErrorTotal(studentListener.getErrorList().size());
        returnVo.setInsertTotal(studentListener.getInsertTotal());
        returnVo.setUpdateTotal(studentListener.getUpdateTotal());
        returnVo.setErrorDetails(studentListener.getErrorList());
        return returnVo;
    }
    /**
     * Helper method to get the '学生' role ID.
     * @return Role ID for '学生'
     */
    private Long getStudentRoleId() {
        MyLambdaQueryWrapper<SysTbrole> queryWrapper = new MyLambdaQueryWrapper<>();
        queryWrapper.eq(SysTbrole::getRoleName, "学生");
        SysTbrole studentRole = sysRoleMapper.selectOne(queryWrapper);
        return studentRole != null ? studentRole.getId() : null;
    }


    public void saveBatchUsers(List<SysTbuser> users) {
        sysUserService.saveBatch(users);
    }

    @Override
    public SysTbuser getStudentUser(StudentReturnVo param) {
        if (ObjectUtils.isEmpty(param.getUserId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该学生未绑定用户");
        }
        MyLambdaQueryWrapper<SysTbuser> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysTbuser::getId, param.getUserId());
        return sysUserService.getOne(lambdaQueryWrapper);
    }

    @Override
    public List<StudentReturnVo> getStudentNoUser() {
        MyLambdaQueryWrapper<Student> queryWrapper = new MyLambdaQueryWrapper<>();
        queryWrapper.isNull(Student::getUserId);

        List<Student> studentList = this.list(queryWrapper);

        // 映射为 TeacherReturnVo（假设结构一致，否则请手动赋值）
        return studentList.stream().map(student -> {
            StudentReturnVo vo = new StudentReturnVo();
            BeanUtils.copyProperties(student, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public GenerateUserResultVoStudent generateStudentUsers(StudentGenerateDto dto) {
        int successCount = 0;
        int failCount = 0;
        List<String> failedStudents = new ArrayList<>();

        for (StudentReturnVo studentReturnVo : dto.getStudent()) {
            try {
                if (studentReturnVo.getUserId() != null) {
                    continue; // 已绑定用户，跳过
                }

                String identityId = studentReturnVo.getIdentityId();
                if (identityId == null || identityId.trim().isEmpty()) {
                    failCount++;
                    failedStudents.add(studentReturnVo.getName() + "(工号为空)");
                    continue;
                }

                // 构造用户
                SysTbuser user = new SysTbuser()
                        .setUsername(identityId)
                        .setPassword(MD5Utils.sha256(identityId))
                        .setUserType(1) // 学生
                        .setInstitutionId(dto.getInstitutionId())
                        .setStatus(1); // 默认启用

                sysUserMapper.insert(user);

                // 更新 teacher 的 user_id
                studentReturnVo.setUserId(user.getId());
                Student student = new Student();
                BeanUtils.copyProperties(studentReturnVo, student);
                studentMapper.updateById(student);

                successCount++;
            } catch (Exception e) {
                failCount++;
                failedStudents.add(studentReturnVo.getName() + "(异常：" + e.getMessage() + ")");
            }
        }

        boolean allSuccess = failCount == 0;

        GenerateUserResultVoStudent resultVo = new GenerateUserResultVoStudent();
        resultVo.setAllSuccess(allSuccess);
        resultVo.setSuccessCount(successCount);
        resultVo.setFailCount(failCount);
        resultVo.setTotalProcessed(dto.getStudent().size());
        resultVo.setFailedStudents(failedStudents);
        return resultVo;
    }

    @Override
    public Boolean getStudentIsInInstitution(GetStudentIsInInstitutionParam param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getStudentId()) || ObjectUtils.isEmpty(param.getInstitutionId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }

        // 获取所有子机构（含自身）
        List<Institution> institutions = institutionMapper.selectList(null);
        List<Long> allSubInstitutionIds = InstitutionUtil.getAllSubInstitutionIds(param.getInstitutionId(), institutions);

        // 查询教师对应的机构ID（通过teacher.user_id -> sys_tbuser.institution_id）
        Long institutionId = studentMapper.getInstitutionIdByStudentId(param.getStudentId());

        if (institutionId == null) {
            return false;  // 学生没有绑定机构，直接false
        }

        return allSubInstitutionIds.contains(institutionId);
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer accountStatus) {
        UpdateWrapper<Student> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids); // WHERE id IN (...)
        updateWrapper.set("account_status", accountStatus); // SET account_status = ?
        // 2. 调用 baseMapper 的 update 方法执行，无需在 XML 中写 SQL
        this.baseMapper.update(null, updateWrapper);
    }



    @Override
    public void exportStudentData(HttpServletResponse response, StudentExportDto param) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try {
            // 文件名设置
            String fileName = URLEncoder.encode("学生信息表", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 1. 查询学生数据
            List<StudentReturnVo> students = studentMapper.selectStudentExportList(param);

            // 2. 将查询结果转换成导出VO列表 (此部分逻辑已更新)
            List<com.upc.modular.student.controller.param.excel.StudentExportExcelVO> exportList = students.stream().map(s -> {
                com.upc.modular.student.controller.param.excel.StudentExportExcelVO vo = new com.upc.modular.student.controller.param.excel.StudentExportExcelVO();

                // --- 按新的VO进行字段映射 ---
                vo.setIdentityId(s.getIdentityId());
                vo.setName(s.getName());
                vo.setGender(s.getGender());
                vo.setCollege(s.getCollege());
                vo.setClassName(s.getClassName());
                vo.setMajor(s.getMajor());
                vo.setIdcard(s.getIdcard());
                vo.setBirthday(s.getBirthday());
                vo.setEmail(s.getEmail());
                vo.setPhone(s.getPhone());

                // 【核心修改】调用转换方法来设置账号状态
                vo.setAccountStatus(convertStudentStatus(s.getAccountStatus()));

                vo.setPosition(s.getPosition());
                vo.setEnrollmentData(s.getEnrollmentData());
                vo.setPlannedGraduationDate(s.getPlannedGraduationDate());
                vo.setRemark(s.getRemark());


                return vo;
            }).collect(Collectors.toList());

            // 3. 利用 EasyExcel 写出Excel
            EasyExcel.write(response.getOutputStream(), com.upc.modular.student.controller.param.excel.StudentExportExcelVO.class)
                    .registerConverter(new LocalDateTimeConverter())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("学生列表")
                    .doWrite(exportList);

        } catch (Exception e) {
            log.error("导出学生信息失败", e); // 建议使用日志记录异常
            // 重置response, 告诉浏览器请求出错了
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            // 这里可以返回一个JSON错误信息，但通常在Controller层通过全局异常处理器完成
            throw new RuntimeException("导出失败，请重试");
        }
    }

    /**
     * 【新增】私有辅助方法：转换学生状态
     * @param status 状态码
     * @return 状态描述文本
     */
    private String convertStudentStatus(Integer status) {
        if (status == null) {
            return "未知";
        }
        // TODO: 请根据您项目中的字典 (studentStatus) 定义来调整这里的映射关系
        switch (status) {
            case 1:
                return "正常";
            case 0:
                return "禁用";
            case 2:
                return "锁定";
            default:
                return "未知状态";
        }
    }

    @Override
    public List<StudentUserResultParam> getStudent(IdParam idParam) {
        MyLambdaQueryWrapper<Student> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        List<Long> idList = idParam.getIdList();

        if (idList != null && !idList.isEmpty()) {
            if (idList.size() == 1) {
                lambdaQueryWrapper.eq(Student::getUserId, idList.get(0));
            } else {
                lambdaQueryWrapper.in(Student::getUserId, idList);
            }
        }
        List<Student> students = studentMapper.selectList(lambdaQueryWrapper);
        List<StudentUserResultParam> resultParam = new ArrayList<>();
        for(Student student : students) {
            StudentUserResultParam param = new StudentUserResultParam();
            BeanUtils.copyProperties(student, param);
            SysLog sysLog = sysLogMapper.selectOne(
                    new MyLambdaQueryWrapper<SysLog>()
                            .eq(SysLog::getUserId, student.getUserId())
                            .eq(SysLog::getLogContent, "/sys-user/login")
                            .orderByDesc(SysLog::getAddDatetime)
                            .last("LIMIT 1")
            );
            param.setLastLoginTime(sysLog.getAddDatetime());
            resultParam.add(param);
        }
        return resultParam;
    }

    @Override
    public R resetStudentPassword(String identityId) {
        if (ObjectUtils.isEmpty(identityId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        MyLambdaQueryWrapper<Student> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Student::getIdentityId, identityId);
        Student student = this.getOne(lambdaQueryWrapper);
        return sysUserService.resetPassword(student.getUserId());
    }


}
