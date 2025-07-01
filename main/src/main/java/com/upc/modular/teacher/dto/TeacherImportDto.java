package com.upc.modular.teacher.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class TeacherImportDto {
    @ApiModelProperty("工号")
    @ExcelProperty("工号")
    private String identityId;

    @ApiModelProperty("身份证号")
    @ExcelProperty("身份证号")
    private String idcard;

    @ApiModelProperty("姓名")
    @ExcelProperty("姓名")
    private String name;

    @ApiModelProperty("民族")
    @ExcelProperty("民族")
    private String nationality;

    @ApiModelProperty("职务")
    @ExcelProperty("职务")
    private String position;

    @ApiModelProperty("职称")
    @ExcelProperty("职称")
    private String professionalTitle;

    @ApiModelProperty("邮箱")
    @ExcelProperty("邮箱")
    private String email;

    @ApiModelProperty("电话")
    @ExcelProperty("电话")
    private String phone;

    @ApiModelProperty("介绍")
    @ExcelProperty("介绍")
    private String introduction;
}
