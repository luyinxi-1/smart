package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.param.SysUserPageSearchParam;
import com.upc.modular.auth.param.UserLoginParam;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.IUserRoleListService;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
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

        String token = UUID.randomUUID().toString().replace("-", "_");

        redisTemplate.opsForValue().set(token, userInfo, Duration.ofHours(2));

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
        lambdaQueryWrapper.like(ObjectUtils.isNotEmpty(param.getCollege()), SysTbuser::getCollege, param.getCollege())
                .eq(ObjectUtils.isNotEmpty(param.getUserType()), SysTbuser::getUserType, param.getUserType())
                .eq(ObjectUtils.isNotEmpty(param.getGender()), SysTbuser::getGender, param.getGender())
                .eq(ObjectUtils.isNotEmpty(param.getIdcard()), SysTbuser::getIdcard, param.getIdcard())
                .like(ObjectUtils.isNotEmpty(param.getName()), SysTbuser::getName, param.getName());
        return this.page(page, lambdaQueryWrapper);
    }

    @Override
    public void insert(SysTbuser sysTbuser) {
        if (ObjectUtils.isEmpty(sysTbuser.getIdentityId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户身份证号不能为空");
        }
        MyLambdaQueryWrapper<SysTbuser> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysTbuser::getIdentityId, sysTbuser.getIdentityId());
        SysTbuser one = this.getOne(lambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(one)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户身份证号已存在");
        }
        this.save(sysTbuser);
    }

}
