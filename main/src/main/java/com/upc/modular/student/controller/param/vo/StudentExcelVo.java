package com.upc.modular.student.controller.param.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StudentExcelVo {

    @ExcelProperty("学号")
    private String identityId;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("学院")
    private String college;

    @ExcelProperty("班级名称")
    private String className;

    @ExcelProperty("所属机构")
    private String institutionName;
}
