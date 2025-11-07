package com.upc.config.web.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全头拦截器，用于设置通用的安全响应头
 * 主要防止点击劫持等安全问题
 */
public class SecurityHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 设置 X-Frame-Options 头，防止点击劫持攻击
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        
        // 设置 Content-Security-Policy 头，限制 iframe 嵌套
        response.setHeader("Content-Security-Policy", "frame-ancestors 'self'");
        
        return true;
    }
}