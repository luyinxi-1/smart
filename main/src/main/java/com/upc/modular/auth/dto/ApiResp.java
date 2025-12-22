package com.upc.modular.auth.dto;

import lombok.Data;

//B的
@Data
public class ApiResp<T> {
    private Integer code;
    private String message;
    private T data;
}

