package com.upc.modular.auth.dto;

import lombok.Data;

@Data
public class RemoteStudentDTO {

    private String studentNo;     // 学号 -> username, identity_id
    private String name;          // 姓名
    private String idCard;        // 身份证
    private String gender;        // 性别
    private String collegeName;   // 学院 -> college
    private String majorName;     // 专业 -> major
    private Long   classId;       // 班级ID -> class_id
    private Long   unitId;        // 单位号 -> institution_id
    private String email;
    private String phone;
    private Integer status;       // 1=正常 0=停用 -> status/account_status
}
