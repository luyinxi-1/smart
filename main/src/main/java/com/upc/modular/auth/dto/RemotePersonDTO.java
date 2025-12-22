package com.upc.modular.auth.dto;

import lombok.Data;

//C用
@Data
public class RemotePersonDTO {

    /**
     * "student" / "teacher"
     */
    private String type;

    // ===== 学生字段（teacher 时通常为 null）=====
    private String studentNo;
    private String collegeName;
    private String majorName;
    private Long classId;

    // ===== 教师字段（student 时通常为 null）=====
    private String jobNo;
    private String nationality;
    private String birthday;
    private String position;
    private String professionalTitle;

    // ===== 公共字段 =====
    private String name;
    private String idCard;
    private String gender;
    private Long unitId;
    private String email;
    private String phone;
    private Integer status;
}
