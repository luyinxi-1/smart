package com.upc.modular.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("同步结果项")
public class SyncItemDTO {

    @ApiModelProperty("sys_tbuser 主键ID")
    private Long userId;

    @ApiModelProperty("业务主键ID（student.id 或 teacher.id）")
    private Long bizId;

    @ApiModelProperty("登录账号（学生=学号，教师=工号）")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("操作类型：INSERT=新增，UPDATE=更新")
    private String opType;
}