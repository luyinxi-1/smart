package com.upc.modular.questionbank.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionImportDTO {

    @ExcelProperty("题型")
    private String typeName; // 对应 Excel: 单选题, 多选题...

    @ExcelProperty("题目属性")
    private String attribute; // 基础知识, 概念...

    @ExcelProperty("难易程度")
    private String difficultyName; // 易, 中等, 难

    @ExcelProperty("题干")
    private String content;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("A选项")
    private String optionA;

    @ExcelProperty("B选项")
    private String optionB;

    @ExcelProperty("C选项")
    private String optionC;

    @ExcelProperty("D选项")
    private String optionD;

    @ExcelProperty("解析")
    private String analysis;
}