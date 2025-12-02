package com.upc.modular.auth.dto;

import lombok.Data;

/**
 * 对应 /api/userinfo 返回的精简用户信息
 */
@Data
public class UserInfoResponse {

    /**
     * 用户唯一标识（subject）
     */
    private String sub;

    /**
     * 签发者（issuer）
     */
    private String iss;

    /**
     * 受众（audience），通常是 client_id
     */
    private String aud;

    /**
     * 邮箱（示例中有这个字段）
     */
    private String email;

    // 如果你们实际返回里有更多字段，也可以再加：
    // private String name;
    // private String phone;
    // 等等，看统一认证那边实际返回为准
}
