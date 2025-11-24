package com.upc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "iam")
public class IamProperties {
    private String host;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;
    private String frontHost;
}