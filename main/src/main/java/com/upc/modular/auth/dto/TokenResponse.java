package com.upc.modular.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("id_token")
    private String idToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("expires_in")
    private String expiresIn;
    
    private String scope;
}