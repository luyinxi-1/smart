package com.upc.modular.student.controller.param.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import com.alibaba.excel.annotation.ExcelProperty; // 1. 导入新的注解
import com.upc.utils.LocalDateTimeConverter; // 2. 导入你的自定义转换器
import lombok.Data;

import java.time.LocalDateTime;
@Data
@ExcelIgnoreUnannotated
public class StudentImportDto {

    @ApiModelProperty("用户学号")
    @ExcelProperty("用户学号")
    private String identityId;

    @ApiModelProperty("身份证号")
    @ExcelProperty("身份证号")
    private String idcard;

    @ApiModelProperty("姓名")
    @ExcelProperty("姓名")
    private String name;

    @ApiModelProperty("班级名称")
    @ExcelProperty("班级名称")
    private String className;

    @ApiModelProperty("联系方式")
    @ExcelProperty("联系方式")
    private String phone;

    @ApiModelProperty("账号状态")
    @ExcelProperty("账号状态")
    private Integer accountStatus;

    @ApiModelProperty("职务")
    @ExcelProperty("职务")
    private String position;

    @ApiModelProperty("性别")
    @ExcelProperty("性别")
    private String gender;

    @ApiModelProperty("入学日期")
    @ExcelProperty(value = "入学日期", converter = LocalDateTimeConverter.class)
    private LocalDateTime enrollmentData;

    @ApiModelProperty("预计结业时间")
    @ExcelProperty(value = "预计结业时间", converter = LocalDateTimeConverter.class)
    private LocalDateTime plannedGraduationDate;

    @ApiModelProperty("电子邮箱")
    @ExcelProperty("电子邮箱")
    private String email;


    @ApiModelProperty("备注")
    @ExcelProperty("备注")
    private String remark;

    @ApiModelProperty("证件照片")
    @ExcelProperty("证件照片")
    private String idPhoto;



}
