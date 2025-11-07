package com.upc.config.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 拦截 /test/** 请求，禁止外部访问测试目录
 */
public class TestAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 拦截所有 /test/** 请求
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Access to /test/ is forbidden.");
        return false; // 拦截，不放行
    }
}
