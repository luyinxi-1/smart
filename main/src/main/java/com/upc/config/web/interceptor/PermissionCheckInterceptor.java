package com.upc.config.web.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.context.LoginContextHolder;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.entity.SysLog;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.mapper.SysAuthorityMapper;
import com.upc.modular.auth.mapper.SysRoleMapper;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.service.ISysLogService;
import com.upc.modular.auth.service.impl.SysAuthorityServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: xth
 * @Date: 2025/8/12 10:21
 */
@Slf4j
public class PermissionCheckInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ISysLogService sysLogService;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysAuthorityMapper sysAuthorityMapper;
    @Autowired
    private SysAuthorityServiceImpl sysAuthorityService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 从上下文中获取用户信息。这是第一个拦截器存放的结果。
        UserInfoToRedis userInfoToRedis = UserUtils.get();

        // 2. 对于需要权限校验的接口，如果在这里发现用户信息为空，
        //    说明用户未提供有效的token，此时必须拦截。
        if (userInfoToRedis == null) {
            throw new BusinessException(BusinessErrorEnum.PLEASE_LOGIN);
        }

        // 3. 调用权限检查方法，并返回检查结果。
        return hasPermission(request, userInfoToRedis);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

    /**
     * 判断当前登录的（用户的）角色信息是否有权访问该地址
     * @param request
     * @param userInfo
     * @return
     */
    private boolean hasPermission(HttpServletRequest request, UserInfoToRedis userInfo) {
        String requestPath = request.getRequestURI();
        // 查询当前登录用户的角色信息
        List<SysTbrole> roles = sysUserMapper.getRolesByUserId(userInfo.getId());
        if (roles.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS);
        }
        List<SysAuthority> SysAuthorities = new ArrayList<>();
        // 一个用户可以绑定多个角色
        for (SysTbrole role : roles) {
            List<SysAuthority> sysAuthorityList = sysAuthorityMapper.getPermissionsByRoleId(role.getId());
            SysAuthorities.addAll(sysAuthorityList);
        }

        if (SysAuthorities.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS);
        }

        boolean pathMatched = this.isPathMatchedInAuth(requestPath, SysAuthorities);

        if (pathMatched) {
            // 记录该访问到日志信息
            if (userInfo.getId() != null && StringUtils.isNotBlank(requestPath)) {
                SysLog sysLog = new SysLog();
                sysLog.setUserId(userInfo.getId());
                sysLog.setLogContent(requestPath);
                if (sysLog != null) {
                    sysLogService.save(sysLog);
                }
            }
            return true;
        } else {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS);
        }

    }

    /**
     * 判断路径是否匹配权限
     * @param requestPath 请求路径
     * @return
     */
    private boolean isPathMatchedInAuth(String requestPath, List<SysAuthority> authorities) {
        for (SysAuthority authority : authorities) {
            String accessUrl = authority.getAccessUrl(); // 从数据库获取的简洁路径，如 /user/info

            if (StringUtils.isBlank(accessUrl)) {
                continue;
            }

            // 核心判断逻辑
            if (requestPath.startsWith(accessUrl)) {
                // 如果请求路径以数据库路径为前缀，我们需要进一步检查边界
                // 1. 如果两个路径完全相等，则匹配成功
                // 2. 如果请求路径更长，那么在数据库路径末尾处的下一个字符必须是'/'
                if (requestPath.length() == accessUrl.length() || requestPath.charAt(accessUrl.length()) == '/') {
                    return true; // 匹配成功
                }
            }
        }
        return false;
    }
}
