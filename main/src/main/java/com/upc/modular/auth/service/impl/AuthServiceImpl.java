package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.config.IamProperties;
import com.upc.modular.auth.client.AuthClient;
import com.upc.modular.auth.dto.AccountResponse;
import com.upc.modular.auth.dto.TokenResponse;
import com.upc.modular.auth.dto.UserDTO;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    public static final String IAM_OAUTH_STATE = "IAM_OAUTH_STATE";
    public static final String LOGIN_USER_ID = "LOGIN_USER_ID";
    public static final String TOKEN_RESPONSE = "TOKEN_RESPONSE";

    @Autowired
    private AuthClient authClient;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IamProperties iamProperties;

    @Override
    public String buildAuthorizeUrl(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute(IAM_OAUTH_STATE, state);
        return authClient.buildAuthorizeUrl(state);
    }

    @Override
    public void handleCallback(String code, String state, HttpSession session) {
        try {
            // 校验state防止CSRF攻击
            String savedState = (String) session.getAttribute(IAM_OAUTH_STATE);
            // 用完就删，防止重复使用
            session.removeAttribute(IAM_OAUTH_STATE);

            if (savedState == null || !savedState.equals(state)) {
                throw new RuntimeException("State校验失败，可能存在CSRF攻击");
            }

            // 获取token
            TokenResponse tokenResponse = authClient.getTokenByCode(code);
            
            // 获取账户信息
            AccountResponse accountResponse = authClient.getAccount(
                    tokenResponse.getTokenType(), 
                    tokenResponse.getAccessToken());
            
            // 新增：检查统一认证返回状态
            if (accountResponse.getStatus() != null
                    && !"ok".equalsIgnoreCase(accountResponse.getStatus())) {
                log.error("统一认证返回非成功状态，status={}, account={}",
                        accountResponse.getStatus(), accountResponse);
                throw new RuntimeException("统一认证获取账户信息失败，status=" + accountResponse.getStatus());
            }

            // 获取或创建本地用户
            SysTbuser user = getOrCreateFromAccount(accountResponse);
            
            // 在session中记录登录状态
            session.setAttribute(LOGIN_USER_ID, user.getId());
            session.setAttribute(TOKEN_RESPONSE, tokenResponse);
        } catch (Exception e) {
            log.error("处理统一认证回调失败", e);
            throw e;
        }
    }

    @Override
    public UserDTO getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(LOGIN_USER_ID);
        if (userId == null) {
            return null;
        }

        SysTbuser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    @Override
    public String logout(HttpSession session) {
        TokenResponse tokenResponse = (TokenResponse) session.getAttribute(TOKEN_RESPONSE);
        session.invalidate();

        String redirectUri;
        if (iamProperties.getFrontHost() != null && !iamProperties.getFrontHost().isEmpty()) {
            // 优先使用 front-host 作为退出后落地页
            redirectUri = iamProperties.getFrontHost();
        } else {
            String raw = iamProperties.getRedirectUri();
            if (raw != null && raw.contains("/sso/callback")) {
                // 常规情况：把 /sso/callback 替换成 /
                redirectUri = raw.replace("/sso/callback", "/");
            } else {
                // 兜底：直接用配置中的 redirect-uri
                redirectUri = raw;
            }
        }

        String state = UUID.randomUUID().toString();
        String idTokenHint = tokenResponse != null ? tokenResponse.getAccessToken() : null;

        return authClient.buildLogoutUrl(idTokenHint, redirectUri, state);
    }
    
    @Override
    public String buildAccountUrl() {
        // iamProperties.getHost() 约定是不带结尾的斜杠，例如 http://iam.xxx.edu.cn
        String host = iamProperties.getHost();
        // 简单处理一下 host 末尾的斜杠，避免出现双斜杠
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return host + "/account";
    }

    /**
     * 根据统一认证账户信息获取或创建本地用户
     * @param account 统一认证账户信息
     * @return 本地用户
     */
    public SysTbuser getOrCreateFromAccount(AccountResponse account) {
        // 根据sub查找用户
        LambdaQueryWrapper<SysTbuser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTbuser::getCasSub, account.getSub());
        SysTbuser user = sysUserMapper.selectOne(queryWrapper);

        if (user != null) {
            // 更新用户信息
            user.setCasName(account.getName());
            if (account.getData() != null && account.getData().containsKey("email")) {
                // 这里可以根据需要更新其他字段
            }
            sysUserMapper.updateById(user);
            return user;
        } else {
            // 创建新用户
            user = new SysTbuser();
            user.setCasSub(account.getSub());
            user.setCasName(account.getName());
            user.setStatus(1); // 默认启用状态
            
            // 从data中获取用户名，如果没有则使用sub
            String username = account.getSub();
            if (account.getData() != null && account.getData().containsKey("username")) {
                username = (String) account.getData().get("username");
            }
            user.setUsername(username);
            user.setNickname(account.getName());
            
            sysUserMapper.insert(user);
            return user;
        }
    }
}