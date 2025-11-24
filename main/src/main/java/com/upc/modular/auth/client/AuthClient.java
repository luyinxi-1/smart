package com.upc.modular.auth.client;

import com.upc.config.IamProperties;
import com.upc.modular.auth.dto.AccountResponse;
import com.upc.modular.auth.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AuthClient {

    @Autowired
    private IamProperties iamProperties;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 构建授权URL
     * @param state 状态码
     * @return 授权URL
     */
    public String buildAuthorizeUrl(String state) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(iamProperties.getHost())
                .append("/login/oauth/authorize?")
                .append("client_id=").append(iamProperties.getClientId()).append("&")
                .append("response_type=code&")
                .append("redirect_uri=");
        
        try {
            urlBuilder.append(URLEncoder.encode(iamProperties.getRedirectUri(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败", e);
            urlBuilder.append(iamProperties.getRedirectUri());
        }
        
        urlBuilder.append("&")
                .append("scope=").append(iamProperties.getScope()).append("&")
                .append("state=").append(state);

        return urlBuilder.toString();
    }

    /**
     * 通过授权码获取token
     * @param code 授权码
     * @return TokenResponse
     */
    public TokenResponse getTokenByCode(String code) {
        String url = iamProperties.getHost() + "/api/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", iamProperties.getClientId());
        map.add("client_secret", iamProperties.getClientSecret());
        map.add("grant_type", "authorization_code");
        map.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<TokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, TokenResponse.class);
        TokenResponse body = response.getBody();
        
        // 空值检查
        if (body == null) {
            log.error("获取token失败，响应体为空");
            throw new RuntimeException("获取token失败，响应体为空");
        }
        
        if (body.getAccessToken() == null) {
            log.error("获取token失败，access_token为空");
            throw new RuntimeException("获取token失败，access_token为空");
        }
        
        return body;
    }

    /**
     * 获取账户信息
     * @param tokenType token类型
     * @param accessToken 访问令牌
     * @return AccountResponse
     */
    public AccountResponse getAccount(String tokenType, String accessToken) {
        String url = iamProperties.getHost() + "/api/get-account";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", tokenType + " " + accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<AccountResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, AccountResponse.class);
        AccountResponse body = response.getBody();
        
        // 空值检查
        if (body == null) {
            log.error("获取账户信息失败，响应体为空");
            throw new RuntimeException("获取账户信息失败，响应体为空");
        }
        
        return body;
    }

    /**
     * 构建登出URL
     * @param idTokenHint ID token
     * @param redirectUri 重定向URI
     * @param state 状态码
     * @return 登出URL
     */
    public String buildLogoutUrl(String idTokenHint, String redirectUri, String state) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(iamProperties.getHost());

        if (idTokenHint != null && !idTokenHint.isEmpty()) {
            // 删除 token：/api/login/oauth/logout?id_token_hint=...&post_logout_redirect_uri=...&state=...
            urlBuilder.append("/api/login/oauth/logout?")
                      .append("id_token_hint=").append(idTokenHint)
                      .append("&post_logout_redirect_uri=");
            try {
                urlBuilder.append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                log.error("URL编码失败", e);
                urlBuilder.append(redirectUri);
            }
            urlBuilder.append("&state=").append(state);
        } else {
            // 清除 session：/logout?redirect_uri=...
            urlBuilder.append("/logout?redirect_uri=");
            try {
                urlBuilder.append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                log.error("URL编码失败", e);
                urlBuilder.append(redirectUri);
            }
            // /logout 这条接口文档中没有 state 参数，这里不再追加 state
        }

        return urlBuilder.toString();
    }
}