package com.upc.modular.auth.controller;

import com.upc.common.responseparam.R;
import com.upc.config.IamProperties;
import com.upc.modular.auth.dto.UserDTO;
import com.upc.modular.auth.service.AuthService;
import com.upc.modular.auth.service.impl.AuthServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/sso")
@Api(tags = "统一身份认证")
public class SsoController {

    @Autowired
    private AuthService authService;
    @Autowired
    private IamProperties iamProperties;

    /**
     * 统一认证登录入口
     * @param session 会话
     * @param response 响应
     * @throws IOException IO异常
     */
    @ApiOperation("登录入口")
    @GetMapping("/login")
    public void login(HttpSession session, HttpServletResponse response) throws IOException {
        String authorizeUrl = authService.buildAuthorizeUrl(session);
        response.sendRedirect(authorizeUrl);
    }

    /**
     * 统一认证回调地址
     * @param code 授权码
     * @param state 状态码
     * @param session 会话
     * @param response 响应
     * @throws IOException IO异常
     */
    @ApiOperation("回调地址")
    @GetMapping("/callback")
    public void callback(@RequestParam String code,
                         @RequestParam String state,
                         HttpSession session,
                         HttpServletResponse response) throws IOException {
        try {
            authService.handleCallback(code, state, session);
            // 重定向到首页
            //response.sendRedirect("/");
            //response.sendRedirect("http://60.217.79.210:6190/#/publicHome");
            // 登录成功后跳转到配置的 front-host
            String target = iamProperties.getFrontHost();
            if (!StringUtils.hasText(target)) {
                // 兜底：没有配置就回到根路径
                target = "/";
            }
            response.sendRedirect(target);
        } catch (Exception e) {
            log.error("处理统一认证回调失败", e);
            response.sendRedirect("/login?error=auth_failed");
        }
    }

    /**
     * 获取当前登录用户信息
     * @param session 会话
     * @return 用户信息
     */
    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/me")
    public R<UserDTO> getCurrentUser(HttpSession session) {
        UserDTO userDTO = authService.getCurrentUser(session);
        if (userDTO == null) {
            return R.unauthorized();
        }
        return R.ok(userDTO);
    }

    /**
     * 统一登出
     * @param session 会话
     * @param response 响应
     * @throws IOException IO异常
     */
    @ApiOperation("统一登出")
    @GetMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) throws IOException {
        try {
            String logoutUrl = authService.logout(session);
            response.sendRedirect(logoutUrl);
        } catch (Exception e) {
            log.error("处理统一登出失败", e);
            // 发生错误时重定向到登录页
            response.sendRedirect("/login?error=logout_failed");
        }
    }
    
    /**
     * 跳转到统一认证用户信息界面
     * @param response 响应
     * @param session 会话
     * @throws IOException IO异常
     */
    @ApiOperation("跳转到统一认证用户信息界面")
    @GetMapping("/account")
    public void account(HttpServletResponse response, HttpSession session) throws IOException {
        // 检查本系统是否已登录
        Long userId = (Long) session.getAttribute(AuthServiceImpl.LOGIN_USER_ID);
        if (userId == null) {
            // 未登录时重定向到 /sso/login
            response.sendRedirect("/sso/login");
            return;
        }
        
        String accountUrl = authService.buildAccountUrl();
        response.sendRedirect(accountUrl);
    }
}