package com.upc.modular.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AccountResponse {
    private String sub;
    private String name;
    
    @JsonProperty("status")
    private String status;
    
    private Map<String, Object> data;
    private Map<String, Object> data2;
}