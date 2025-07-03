package com.upc.modular.student.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.student.controller.param.dto.StudentGenerateDto;
import com.upc.modular.student.controller.param.dto.StudentImportDto;
import com.upc.modular.student.controller.param.dto.StudentPageSearchDto;
import com.upc.modular.student.controller.param.listener.StudentListener;
import com.upc.modular.student.controller.param.vo.GenerateUserResultVoStudent;
import com.upc.modular.student.controller.param.vo.ImportStudentReturnVo;
import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.student.service.IStudentService;
import com.upc.utils.MD5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private ISysUserService sysUserService;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public void insertstudent(Student student) {
        if (ObjectUtils.isEmpty(student.getIdcard())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户身份证号为空");
        }
        MyLambdaQueryWrapper<Student> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Student::getIdcard, student.getIdcard());
        List<Student> students = studentMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(students)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户已存在");
        }
        this.save(student);
    }

    @Override
    public void deleteByIds(IdParam idParam) {
        List<Long> idList = idParam.getIdList();
        if (ObjectUtils.isEmpty(idList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }

        // 查询教师记录
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

    @Override
    public ImportStudentReturnVo importStudentData(MultipartFile file) {
        ExcelReader excelReader = null;
        List<Student> students = studentMapper.selectList(null);
        ImportStudentReturnVo importStudentReturnparam = new ImportStudentReturnVo();
        try {
            StudentListener studentListener = new StudentListener(this, students);
            excelReader = EasyExcel.read(file.getInputStream(), StudentImportDto.class, studentListener).build();
            // 这两行用于执行读操作，不加不运行
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            excelReader.read(readSheet);
            importStudentReturnparam.setUpdateTotal(studentListener.getUpdateTotal());
            importStudentReturnparam.setInsertTotal(studentListener.getInsertTotal());
            return importStudentReturnparam;
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
    public SysTbuser getStudentUser(StudentReturnVo param) {
        if (ObjectUtils.isEmpty(param.getUserId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该教师未绑定用户");
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
                        .setUserType("1") // 学生
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


}
