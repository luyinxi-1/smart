package com.upc.modular.group.controller.param;

import lombok.Data;

@Data
public class UserTypeCount {
    private Integer type; // 0: 管理员, 1: 学生, 2: 老师
    private Long count;
}
