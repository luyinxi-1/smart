package com.upc.modular.auth.service;

import com.upc.modular.auth.dto.UserDTO;

import javax.servlet.http.HttpSession;

public interface AuthService {
    /**
     * 构建授权URL
     * @param session 会话
     * @return 授权URL
     */
    String buildAuthorizeUrl(HttpSession session);

    /**
     * 处理回调
     * @param code 授权码
     * @param state 状态码
     * @param session 会话
     */
    void handleCallback(String code, String state, HttpSession session);

    /**
     * 获取当前用户
     * @param session 会话
     * @return 用户信息
     */
    UserDTO getCurrentUser(HttpSession session);

    /**
     * 登出
     * @param session 会话
     * @return 登出重定向URL
     */
    String logout(HttpSession session);
    
    /**
     * 构建统一认证的用户信息页面 URL
     * 对应统一认证文档中的 <IAM_HOST>/account
     */
    String buildAccountUrl();
}