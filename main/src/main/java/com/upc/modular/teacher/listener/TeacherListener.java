package com.upc.modular.teacher.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
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
import com.upc.modular.teacher.dto.TeacherImportErrorDto;
import com.upc.utils.AgeQuantifyUtils;
import com.upc.utils.MD5Utils;
import com.upc.utils.TypeConversionUtils;
import com.upc.modular.teacher.dto.TeacherImportDto;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.ITeacherService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class TeacherListener extends AnalysisEventListener<TeacherImportDto> {

    private static final int BATCH_COUNT = 3000;

    private final Map<String, Teacher> existTeacherMap;

    private final ITeacherService teacherService;

    private final SysUserMapper sysUserMapper;


    private final IUserRoleListService userRoleListService;

    private final Map<String, Long> institutionMap;

    private final Long roleId;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;


    /**
     * 新增数据列表
     */
    List<Teacher> teacherList = new ArrayList<>(BATCH_COUNT);
    /**
     * 更新数据列表
     */
    List<Teacher> teacherUpdateList = new ArrayList<>(BATCH_COUNT);

    @Getter
    List<TeacherImportErrorDto> errorList = new ArrayList<>();

    /**
     * 新增数据条数
     */
    @Getter
    private long insertTotal;

    /**
     * 更新数据条数
     */
    @Getter
    private long updateTotal;

    List<UserRoleList> userRoleList = new ArrayList<>(BATCH_COUNT);

    public TeacherListener(ITeacherService teacherService, List<Teacher> existTeacherMap, SysUserMapper sysUserMapper, Map<String, Long> institutionMap, IUserRoleListService userRoleListService, Long id) {
        this.userRoleListService = userRoleListService;
        this.institutionMap = institutionMap;
        this.roleId = id;
        this.sysUserMapper = sysUserMapper;
        this.teacherService = teacherService;
        this.existTeacherMap = existTeacherMap.stream()
                .filter(user -> StringUtils.isNotBlank(user.getIdentityId()))
                .collect(Collectors.toMap(
                        Teacher::getIdentityId,
                        Function.identity(), // value 就是这个 user 本身
                        (oldVal, newVal) -> oldVal // 如果身份证重复，保留第一个
                ));
        this.threadPoolTaskExecutor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
    }
    @Override
    @Transactional
    public void invoke(TeacherImportDto teacherImportDto, AnalysisContext analysisContext) {
        String idcard = teacherImportDto.getIdcard();
        TeacherImportErrorDto errorDto = new TeacherImportErrorDto();
        if (StringUtils.isBlank(idcard) || idcard.length() < 14) {
            log.warn("跳过无效数据，身份证号格式错误：{}", idcard);
            BeanUtils.copyProperties(teacherImportDto, errorDto);
            errorDto.setErrorReason("身份证号错误");
            errorList.add(errorDto);
            return;
        }
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherImportDto, teacher);
        String dateBirth = null;
        String newGender = null;
        try {
            // 尝试提取出生日期和性别
            dateBirth = AgeQuantifyUtils.getBirthDateFromIdNumber(teacherImportDto.getIdcard());
            newGender = TypeConversionUtils.sexToString(AgeQuantifyUtils.getGenderFromIdNumber(teacherImportDto.getIdcard()));
        } catch (Exception e) {
            // 捕获异常并记录错误信息
            log.error("处理身份证号 {} 时出错，错误信息：{}", idcard, e.getMessage());
            BeanUtils.copyProperties(teacherImportDto, errorDto);
            errorDto.setErrorReason("身份证号格式错误");
            errorList.add(errorDto);
            return;  // 跳过当前数据
        }
        Integer newIsPartyNumber = TypeConversionUtils.isPartyNumberToInteger(teacherImportDto.getIsPartyNumber());
        Integer newEducationalBackground = TypeConversionUtils.educationalBackgroundToInteger(teacherImportDto.getEducationalBackground());

        teacher.setBirthday(dateBirth);
        teacher.setGender(newGender);
        teacher.setIsPartyNumber(newIsPartyNumber);
        teacher.setEducationalBackground(newEducationalBackground);

        // 用 map 判断是否存在
        Teacher existTeacher = existTeacherMap.get(teacher.getIdentityId());

        if (existTeacher != null) {
            teacher.setId(existTeacher.getId()); // 设置 ID 用于更新
            teacherUpdateList.add(teacher);
            updateTotal++;
        } else {
            SysTbuser user = new SysTbuser()
                    .setUsername(teacher.getIdentityId())
                    .setPassword(MD5Utils.sha256(teacher.getIdentityId()))
                    .setUserType("2") // 教师
                    .setStatus(1); // 默认启用
            if (ObjectUtils.isNotEmpty(teacherImportDto.getInstitutionName()) && ObjectUtils.isNotEmpty(institutionMap.get(teacherImportDto.getInstitutionName()))) {
                user.setInstitutionId(institutionMap.get(teacherImportDto.getInstitutionName()));
            } else {
                BeanUtils.copyProperties(teacherImportDto, errorDto);
                errorDto.setErrorReason("未查询到机构");
                errorList.add(errorDto);
            }
            try {
                sysUserMapper.insert(user);  // Try to insert the user
                teacher.setUserId(user.getId());
                if (roleId != 0L) {
                    UserRoleList userOrRole = new UserRoleList()
                            .setRoleId(roleId)
                            .setUserId(user.getId());
                    userRoleList.add(userOrRole);
                }
                teacherList.add(teacher);
                insertTotal++;
            } catch (Exception e) {
                // 捕获异常，记录日志，并在 errorList 中添加错误信息
                log.error("插入教师用户失败，身份证号：{}，错误信息：{}", teacher.getIdentityId(), e.getMessage());
                BeanUtils.copyProperties(teacherImportDto, errorDto);
                errorDto.setErrorReason("插入教师用户失败：" + e.getMessage());
                errorList.add(errorDto);
                return;  // 跳过此条数据
            }
        }

        if (teacherList.size() >= BATCH_COUNT || teacherUpdateList.size() >= BATCH_COUNT) {
            this.doSaveDataAsync();
            teacherList.clear();
            teacherUpdateList.clear();
            userRoleList.clear();
        }
    }

    private void doSaveDataAsync() {
        Future<?> future = threadPoolTaskExecutor.submit(this::saveDataAsync);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "导入失败");
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        this.saveDataAsync();
        if (insertTotal > 3000) {
            log.warn("导入总数为：{}", insertTotal);
        } else {
            log.info("导入总数为：{}", insertTotal);
        }
    }


    private void saveDataAsync() {
        // 批量保存教师数据
        if (CollectionUtils.isNotEmpty(teacherList)) {
            try {
                teacherService.saveBatch(teacherList);
            } catch (Exception e) {
                // 捕获整个批次的异常，并记录失败的数据
                log.error("批量保存教师数据失败，错误信息：{}", e.getMessage());
//                List<Long> userIdsToDelete = teacherList.stream()
//                        .map(Teacher::getUserId)  // 获取每个 Teacher 对象的 userId
//                        .collect(Collectors.toList());  // 收集为 List
//                sysUserMapper.deleteBatchIds(userIdsToDelete);
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "导入失败");
            }
        }

        // 批量保存角色数据
        if (ObjectUtils.isNotEmpty(userRoleList)) {
            try {
                userRoleListService.saveBatch(userRoleList);
            } catch (Exception e) {
                // 捕获整个批次的异常，并记录失败的数据
                log.error("批量保存用户角色数据失败，错误信息：{}", e.getMessage());
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "导入失败");
            }
        }

        // 批量更新教师数据
        if (CollectionUtils.isNotEmpty(teacherUpdateList)) {
            try {
                teacherService.updateBatchById(teacherUpdateList);
            } catch (Exception e) {
                // 捕获整个批次的异常，并记录失败的数据
                log.error("批量更新教师数据失败，错误信息：{}", e.getMessage());
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "导入失败");
            }
        }

        log.info("导入成功");
    }

}

