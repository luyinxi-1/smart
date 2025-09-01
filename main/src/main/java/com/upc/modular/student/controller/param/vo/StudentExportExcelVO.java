
package com.upc.modular.student.controller.param.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class StudentExportExcelVO {


    @ExcelProperty("班级名称")
    @ApiModelProperty("班级名称")
    private String className;


    @ExcelProperty("学号")
    @ApiModelProperty("用户学号")
    private String identityId;

    @ExcelProperty("身份证号")
    @ApiModelProperty("身份证号")
    private String idcard;

    @ExcelProperty("姓名")
    @ApiModelProperty("姓名")
    private String name;

    @ExcelProperty("性别")
    @ApiModelProperty("性别")
    private String gender;

    @ExcelProperty("学院")
    @ApiModelProperty("学院")
    private String college;

    @ExcelProperty("生日")
    @ApiModelProperty("生日")
    private String birthday;

    @ExcelProperty("电子邮箱")
    @ApiModelProperty("电子邮箱")
    private String email;

    @ExcelProperty("电话")
    @ApiModelProperty("电话")
    private String phone;


    @ExcelProperty("账号状态")
    @ApiModelProperty("账号状态")
    private String accountStatus;

    @ExcelProperty("职务")
    @ApiModelProperty("职务")
    private String position;

    @ExcelProperty(value = "入学日期", converter = com.upc.modular.student.converter.LocalDateTimeConverter.class)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("入学日期")
    private LocalDateTime enrollmentData;

    @ExcelProperty(value = "预计结业时间", converter = com.upc.modular.student.converter.LocalDateTimeConverter.class)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("预计结业时间")
    private LocalDateTime plannedGraduationDate;

    @ExcelProperty("备注")
    @ApiModelProperty("备注")
    private String remark;

    @ExcelProperty("专业")
    @ApiModelProperty("专业")
    private String major;




}
