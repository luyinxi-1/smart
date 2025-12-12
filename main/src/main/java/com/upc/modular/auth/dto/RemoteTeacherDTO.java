package com.upc.modular.auth.dto;
import lombok.Data;

@Data
public class RemoteTeacherDTO {

    private String jobNo;            // 工号 -> username, identity_id
    private String name;             // 姓名
    private String idCard;           // 身份证
    private String gender;           // 性别
    private String nationality;      // 民族
    private String birthday;         // 生日
    private String position;         // 职务
    private String professionalTitle;// 职称
    private String email;
    private String phone;
    private Long   unitId;           // 单位号 -> institution_id
    private Integer status;          // 1=在职 0=停用
}
