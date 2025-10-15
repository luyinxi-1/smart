package com.upc.modular.auth.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.mapper.UserRoleListMapper;
import com.upc.modular.auth.param.*;
import com.upc.modular.auth.service.ISysLogService;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.IUserRoleListService;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.mapper.InstitutionMapper;
import com.upc.utils.AesCbcCompatUtil;
import com.upc.utils.InstitutionUtil;
import com.upc.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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

        redisTemplate.opsForValue().set(token, userInfo, Duration.ofHours(24));

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

        // 统一处理通用查询条件：昵称模糊查询
        lambdaQueryWrapper.like(ObjectUtils.isNotEmpty(param.getNickname()), SysTbuser::getNickname, param.getNickname());

        // 根据不同的userType处理查询逻辑
        if (ObjectUtils.isNotEmpty(param.getUserType())) {
            if (param.getUserType() == -1) {
                // userType为-1是特殊标识，查询类型为1（教师）或2（学生）的用户
                lambdaQueryWrapper.and(w -> w.eq(SysTbuser::getUserType, 1)
                        .or()
                        .eq(SysTbuser::getUserType, 2));
            } else {
                // 其他情况，直接按传入的userType精确查询
                lambdaQueryWrapper.eq(SysTbuser::getUserType, param.getUserType());
            }
        }
        // 如果param.getUserType()为空，则不添加用户类型的筛选，实现查询所有类型的用户

        // 统一处理排序
        lambdaQueryWrapper.orderBy(true, param.getIsAsc() == 1, SysTbuser::getAddDatetime);

        // 执行分页查询
        Page<SysTbuser> resultPage = this.page(page, lambdaQueryWrapper);

        // 填充创建人姓名
        List<SysTbuser> userList = resultPage.getRecords();
        if (ObjectUtils.isNotEmpty(userList)) {
            // 1. 收集所有创建人的ID
            Set<Long> creatorIds = userList.stream()
                    .map(SysTbuser::getCreator)
                    .filter(ObjectUtils::isNotEmpty)
                    .collect(Collectors.toSet());

            if (ObjectUtils.isNotEmpty(creatorIds)) {
                // 2. 一次性查询出所有创建人的信息
                List<SysTbuser> creators = this.listByIds(creatorIds);
                Map<Long, String> creatorMap = creators.stream()
                        .collect(Collectors.toMap(SysTbuser::getId, SysTbuser::getNickname));

                // 3. 遍历结果集，设置创建人姓名
                userList.forEach(user -> {
                    if (user.getCreator() != null) {
                        user.setCreatorName(creatorMap.get(user.getCreator()));
                    }
                });
            }
        }

        return resultPage;
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

    @Override
    public Boolean insert(SysTbuser sysTbuser) {
        if (ObjectUtils.isEmpty(sysTbuser) || ObjectUtils.isEmpty(sysTbuser.getUsername())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        SysTbuser sysTbuser1 = sysUserMapper.selectOne(new LambdaQueryWrapper<SysTbuser>().eq(SysTbuser::getUsername, sysTbuser.getUsername()));
        if (ObjectUtils.isNotEmpty(sysTbuser1)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户名已存在");
        }
        if (ObjectUtils.isEmpty(sysTbuser.getUserType())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户类型不能为空");
        }
        if (ObjectUtils.isEmpty(sysTbuser.getPassword())) {
            sysTbuser.setPassword(AesCbcCompatUtil.encryptZeroBase64("Aa123456+"));
        }
        this.save(sysTbuser);
        userRoleListService.insertDefaultRole(sysTbuser.getId(), sysTbuser.getUserType());
        return true;
    }

    @Override
    public R updatePassword(UpdatePasswordParam param) {
        if (StringUtils.isBlank(param.getOldPassword()) || StringUtils.isBlank(param.getNewPassword())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        if (param.getOldPassword().equals(param.getNewPassword())) {
            return R.ok("新旧密码不能重复");
        }

        SysTbuser tbuser;
        Long targetId;
        if (param.getId() == null || param.getId() == 0L) {
            UserInfoToRedis userInfoToRedis = UserUtils.get();
            tbuser = this.getById(userInfoToRedis.getId());

            if (userInfoToRedis == null || tbuser ==  null) {
                throw new BusinessException(BusinessErrorEnum.USER_NO);
            }
            targetId = userInfoToRedis.getId();
        } else {
            tbuser = this.getById(param.getId());
            targetId = tbuser.getId();
        }

        if (!param.getOldPassword().equals(tbuser.getPassword())) {
            return R.ok("旧密码输入错误");
        }

        LambdaUpdateWrapper<SysTbuser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysTbuser::getId, targetId);
        updateWrapper.set(SysTbuser::getPassword, param.getNewPassword());
        boolean update = this.update(updateWrapper);


        if (update) {
            return R.ok("修改成功");
        } else {
            throw new BusinessException(BusinessErrorEnum.MYSQL_ERR);
        }
    }

    @Override
    public R resetPassword(Long userId) {
        if (userId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }

        // 生成一个随机八位数密码
        String newPassword = String.valueOf(ThreadLocalRandom.current().nextInt(10000000, 100000000));
        String secretPassword = AesCbcCompatUtil.encryptZeroBase64(newPassword);

        LambdaUpdateWrapper<SysTbuser> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(SysTbuser::getId, userId);
        updateWrapper.set(SysTbuser::getPassword, secretPassword);
        this.update(updateWrapper);

        return R.ok("新密码是：" + newPassword);
    }

    @Override
    public Long getUserInfo(Long id, Integer userType) {
        if (ObjectUtils.isEmpty(id) || ObjectUtils.isEmpty(userType)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户ID或用户类型为空");
        }
         Long schoolId = sysUserMapper.getUserInfo(id, userType);
         return schoolId;
    }


}
