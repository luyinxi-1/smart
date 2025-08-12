package com.upc.config.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.context.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    /**
     * preHandle方法是进行处理器拦截用的，该方法将在Controller处理之前进行调用
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 0.如果之前的某个环节已经处理过，就直接通过
        if (LoginContextHolder.getIsLogined()) {
            return true;
        }
        // 取出请求头的token
        String token = request.getHeader("token");
        // 1.验证常规token
        if (StringUtils.isBlank(token)) {
            // 因为对于白名单接口，没有 token 也是允许访问的。因为有下一个拦截器兜底。
            return true;
        }
        // 2.检查token合法性和有效性——即使 token 不为空，也可能是伪造、无效、过期的
        // 当token过期redis中取不到数据会抛异常
        // 将用户信息存入线程
        try {
            ValueOperations<String, Map<String, Object>> operation = redisTemplate.opsForValue();
            Map<String, Object> map  = operation.get(token);

            // 即使 map 为空（token过期或无效），我们也不抛异常，让请求继续。
            // 后续的权限拦截器会处理“需要保护但用户为空”的情况。
            if (map != null) {
                UserInfoToRedis userInfoToRedis = JSON.parseObject(JSON.toJSONString(map), UserInfoToRedis.class);
                if (!ObjectUtils.isEmpty(userInfoToRedis)) {
                    // 成功获取到用户信息，将其存入线程
                    UserUtils.set(userInfoToRedis);
                    LoginContextHolder.setLogined(true);
                }
            }

        } catch (IllegalArgumentException e) {
            log.error("Token校验失败：" + e.getMessage());
        }
        // 3.此拦截器永远返回 true，它的职责只是填充上下文，不是拦截。
        return true;
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

}
