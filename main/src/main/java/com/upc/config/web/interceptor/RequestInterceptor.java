package com.upc.config.web.interceptor;

import com.alibaba.fastjson.JSON;
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
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 校验token的拦截器，如果token校验通过就把信息存到redis
 *
 * @author xth
 * 拦截器
 */
@Slf4j
public class RequestInterceptor implements HandlerInterceptor {

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

    /**
     * preHandle方法是进行处理器拦截用的，该方法将在Controller处理之前进行调用
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (LoginContextHolder.getIsLogined()) {
            return true;
        }
        // 取出请求头的token
        String token = request.getHeader("token");
        // 1.验证常规token
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(BusinessErrorEnum.PLEASE_LOGIN);
            // return false;
        }
        // 2.检查token合法性和有效性——即使 token 不为空，也可能是伪造、无效、过期的
        // 当token过期redis中取不到数据会抛异常
        // 将用户信息存入线程
        try {
            ValueOperations<String, Map<String, Object>> operation = redisTemplate.opsForValue();
            Map<String, Object> map  = operation.get(token);
            UserInfoToRedis userInfoToRedis = JSON.parseObject(JSON.toJSONString(map), UserInfoToRedis.class);
            if (ObjectUtils.isEmpty(userInfoToRedis)) {
                throw new BusinessException(BusinessErrorEnum.PLEASE_LOGIN);
            }
            UserUtils.set(userInfoToRedis);
            LoginContextHolder.setLogined(true);
            // 3.权限拦截
             return hasPermission(request, userInfoToRedis);
            // return true;
        } catch (IllegalArgumentException e) {
            log.error("Token校验失败：" + e.getMessage());
            return false;
        }
    }


    /**
     * 该方法也是需要当前对应的拦截器的preHandle方法的返回值为true时才会执行。
     * 该方法将在整个请求完成之后，也就是DispatcherServlet渲染了视图执行。
     * （这个方法的主要作用是用于清理资源的）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserUtils.clear();
        LoginContextHolder.clear();
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
        if (roles.isEmpty() || roles.get(0) == null) {
            throw new BusinessException(BusinessErrorEnum.NOT_PERMISSIONS);
        }
        SysTbrole sysTbrole = roles.get(0);

        List<SysAuthority> SysAuthorities = sysAuthorityMapper.getPermissionsByRoleId(sysTbrole.getId());
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
     * 判断路径是否匹配权限树中的某个节点
     * @param requestPath 请求路径
     * @return
     */
    private boolean isPathMatchedInAuth(String requestPath, List<SysAuthority> SysAuthorities) {
        for (SysAuthority authority : SysAuthorities) {
            String accessUrl = authority.getAccessUrl();
            if (requestPath.equals(accessUrl)) {
                return true;
            }
        }

        return false;
    }

}
