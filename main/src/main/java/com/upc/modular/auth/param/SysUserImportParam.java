package com.upc.modular.auth.param;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class SysUserImportParam {
    @ApiModelProperty("用户名")
    @ExcelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    @ExcelProperty("密码")
    private String password;

    @ApiModelProperty("用户类型")
    @ExcelProperty("用户类型")
    private String userType;

    @ApiModelProperty("学工号码")
    @ExcelProperty("学工号码")
    private String identityId;

    @ApiModelProperty("身份证号")
    @ExcelProperty("身份证号")
    private String idcard;

    @ApiModelProperty("姓名")
    @ExcelProperty("姓名")
    private String name;

    @ApiModelProperty("性别")
    @ExcelProperty("性别")
    private String gender;

    @ApiModelProperty("学院")
    @ExcelProperty("学院")
    private String college;

    @ApiModelProperty("出生日期")
    @ExcelProperty("出生日期")
    private String birthday;

    @ApiModelProperty("电子邮件")
    @ExcelProperty("电子邮件")
    private String email;

    @ApiModelProperty("电话")
    @ExcelProperty("电话")
    private String phone;

    @ApiModelProperty("年龄")
    @ExcelProperty("年龄")
    private Integer age;
}
