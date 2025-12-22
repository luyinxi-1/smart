package com.upc.modular.auth.dto;

public enum RemoteBaseDataMode {
    /**
     * 统一接口返回：{ "students": [...], "teachers": [...] }
     */
    A,

    /**
     * 统一接口返回：{ "code":200, "message":"ok", "data": { "students":[...], "teachers":[...] } }
     */
    B,

    /**
     * 统一接口返回：[{ "type":"student", ... }, { "type":"teacher", ... }]
     */
    C,

    /**
     * 老方式：两个接口 /students 和 /teachers
     */
    SPLIT
}

