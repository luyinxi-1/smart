package com.upc.modular.student.controller.param.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StudentClassCourseExportVO {
    
    @ExcelProperty("班级名称")
    private String className;
    
    @ExcelProperty("学生名称")
    private String studentName;
    
    @ExcelProperty("课程名称")
    private String courseName;
    
    @ExcelProperty("教材名称")
    private String textbookName;
}