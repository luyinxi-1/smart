package com.upc.modular.auth.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.param.*;
import com.upc.modular.auth.service.ISysLogService;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.IUserRoleListService;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.mapper.InstitutionMapper;
import com.upc.utils.InstitutionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysTbuser> implements ISysUserService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IUserRoleListService userRoleListService;

    @Autowired
    private ISysLogService sysLogService;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    private static Integer ENABLE_STATUS = 1;


    @Override
    public String login(UserLoginParam userLogin, HttpServletRequest request) {
        if (userLogin == null || StringUtils.isBlank(userLogin.getUsername()) || StringUtils.isBlank(userLogin.getPassword())) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY);
        }

        LambdaQueryWrapper<SysTbuser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(SysTbuser::getUsername, userLogin.getUsername())
                .eq(SysTbuser::getPassword, userLogin.getPassword());
        SysTbuser userInfo = this.getOne(queryWrapper);
        if (userInfo == null) {
            throw new BusinessException(BusinessErrorEnum.LOGIN_FAIL);
        }
        if (userInfo.getStatus() != ENABLE_STATUS) {
            throw new BusinessException(BusinessErrorEnum.ACCOUNT_BANNED);
        }

        String token = UUID.randomUUID().toString().replace("-", "_");

        redisTemplate.opsForValue().set(token, userInfo, Duration.ofHours(2));

        // 记录该登录信息到日志
        if (userInfo.getId() != null && StringUtils.isNotBlank(request.getRequestURI())) {
            SysLog sysLog = new SysLog();
            sysLog.setUserId(userInfo.getId());
            sysLog.setLogContent(request.getRequestURI());
            if (sysLog != null) {
                sysLogService.save(sysLog);
            }
        }

        return token;
    }

    @Override
    public void batchDelete(List<Long> idList) {
        for (Long id : idList) {
            MyLambdaQueryWrapper<UserRoleList> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserRoleList::getUserId, id);
            userRoleListService.remove(lambdaQueryWrapper);
            this.removeById(id);
        }
    }

    @Override
    public Page<SysTbuser> getPage(SysUserPageSearchParam param) {
        Page<SysTbuser> page = new Page<>(param.getCurrent(), param.getSize());
        MyLambdaQueryWrapper<SysTbuser> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtils.isNotEmpty(param.getUserType()), SysTbuser::getUserType, param.getUserType())
                .orderBy(true, param.getIsAsc() == 1, SysTbuser::getAddDatetime);
        return this.page(page, lambdaQueryWrapper);
    }

    @Override
    public Boolean getUserIsInInstitution(GetUserIsInInstitutionParam param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getInstitutionId()) || ObjectUtils.isEmpty(param.getUserId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }

        // 获取所有子机构（含自身）
        List<Institution> institutions = institutionMapper.selectList(null);
        List<Long> allSubInstitutionIds = InstitutionUtil.getAllSubInstitutionIds(param.getInstitutionId(), institutions);

        // 查询教师对应的机构ID（通过teacher.user_id -> sys_tbuser.institution_id）
        MyLambdaQueryWrapper<SysTbuser> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysTbuser::getId, param.getUserId());
        SysTbuser sysTbuser = sysUserMapper.selectOne(lambdaQueryWrapper);
        Long institutionId = sysTbuser.getInstitutionId();

        if (institutionId == null) {
            return false;  // 教师没有绑定机构，直接false
        }

        return allSubInstitutionIds.contains(institutionId);
    }


}
