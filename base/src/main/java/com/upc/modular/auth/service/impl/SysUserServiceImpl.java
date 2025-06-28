package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysUser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.param.UserLoginParam;
import com.upc.modular.auth.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
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
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public String login(UserLoginParam userLogin, HttpServletRequest request) {
        if (userLogin == null || StringUtils.isBlank(userLogin.getUsername()) || StringUtils.isBlank(userLogin.getPassword())) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY);
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(SysUser::getUsername, userLogin.getUsername())
                .eq(SysUser::getPassword, userLogin.getPassword());
        SysUser userInfo = this.getOne(queryWrapper);
        if (userInfo == null) {
            throw new BusinessException(BusinessErrorEnum.LOGIN_FAIL);
        }

        String token = UUID.randomUUID().toString().replace("-", "_");

        redisTemplate.opsForValue().set(token, userInfo, Duration.ofHours(2));

        return token;
    }
}
