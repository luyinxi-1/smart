package com.upc.modular.teacher.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.mapper.SysRoleMapper;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.mapper.UserRoleListMapper;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.mapper.InstitutionMapper;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.utils.AgeQuantifyUtils;
import com.upc.utils.MD5Utils;
import com.upc.utils.TypeConversionUtils;
import com.upc.modular.teacher.dto.TeacherImportDto;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.ITeacherService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class TeacherListener extends AnalysisEventListener<TeacherImportDto> {

    private static final int BATCH_COUNT = 1000;

    private final Map<String, Teacher> existTeacherMap;

    private final ITeacherService teacherService;

    private final SysUserMapper sysUserMapper;

    private final InstitutionMapper institutionMapper;

    private final UserRoleListMapper userRoleListMapper;

    private final SysRoleMapper sysRoleMapper;

    /**
     * 新增数据列表
     */
    List<Teacher> teacherList = new ArrayList<>(BATCH_COUNT);
    /**
     * 更新数据列表
     */
    List<Teacher> teacherUpdateList = new ArrayList<>(BATCH_COUNT);

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

    public TeacherListener(ITeacherService teacherService, List<Teacher> existTeacherMap, SysUserMapper sysUserMapper, InstitutionMapper institutionMapper, UserRoleListMapper userRoleListMapper, SysRoleMapper sysRoleMapper) {
        this.userRoleListMapper = userRoleListMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserMapper = sysUserMapper;
        this.institutionMapper = institutionMapper;
        this.teacherService = teacherService;
        this.existTeacherMap = existTeacherMap.stream()
                .filter(user -> StringUtils.isNotBlank(user.getIdentityId()))
                .collect(Collectors.toMap(
                        Teacher::getIdentityId,
                        Function.identity(), // value 就是这个 user 本身
                        (oldVal, newVal) -> oldVal // 如果身份证重复，保留第一个
                ));
    }
    @Override
    public void invoke(TeacherImportDto teacherImportDto, AnalysisContext analysisContext) {
        String idcard = teacherImportDto.getIdcard();
        if (StringUtils.isBlank(idcard) || idcard.length() < 14) {
            log.warn("跳过无效数据，身份证号格式错误：{}", idcard);
            return; // 或者记录到错误列表中
        }
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherImportDto, teacher);
        String dateBirth = AgeQuantifyUtils.getBirthDateFromIdNumber(teacherImportDto.getIdcard());
        String newGender = TypeConversionUtils.sexToString(AgeQuantifyUtils.getGenderFromIdNumber(teacherImportDto.getIdcard()));
        Integer newIsPartyNumber = TypeConversionUtils.isPartyNumberToInteger(teacherImportDto.getIsPartyNumber());
        Integer newEducationalBackground = TypeConversionUtils.educationalBackgroundToInteger(teacherImportDto.getEducationalBackground());

        teacher.setBirthday(dateBirth);
        teacher.setGender(newGender);
        teacher.setIsPartyNumber(newIsPartyNumber);
        teacher.setEducationalBackground(newEducationalBackground);

        MyLambdaQueryWrapper<Institution> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtils.isNotEmpty(teacherImportDto.getInstitutionName()), Institution::getInstitutionName, teacherImportDto.getInstitutionName());
        List<Institution> institutions = institutionMapper.selectList(lambdaQueryWrapper);


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
            if (ObjectUtils.isNotEmpty(institutions)) {
                user.setInstitutionId(institutions.get(0).getId());
            }
            sysUserMapper.insert(user);
            teacher.setUserId(user.getId());
            MyLambdaQueryWrapper<SysTbrole> listMyLambdaQueryWrapper = new MyLambdaQueryWrapper<>();
            listMyLambdaQueryWrapper.eq(SysTbrole::getRoleName, "普通教师");
            List<SysTbrole> sysTbroles = sysRoleMapper.selectList(listMyLambdaQueryWrapper);
            if (ObjectUtils.isNotEmpty(sysTbroles)) {
                Long id = sysTbroles.get(0).getId();
                UserRoleList userRoleList = new UserRoleList()
                        .setRoleId(id)
                        .setUserId(user.getId());
                userRoleListMapper.insert(userRoleList);
            }
            teacherList.add(teacher);
            insertTotal++;
        }

        if (teacherList.size() >= BATCH_COUNT || teacherUpdateList.size() >= BATCH_COUNT) {
            this.saveDataAsync();
            teacherList.clear();
            teacherUpdateList.clear();
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
        if (CollectionUtils.isNotEmpty(teacherList)) {
            teacherService.saveBatch(teacherList);
        }
        if (CollectionUtils.isNotEmpty(teacherUpdateList)){
            teacherService.updateBatchById(teacherUpdateList);
        }
        log.info("导入成功");
    }
}

