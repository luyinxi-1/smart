package com.upc.modular.datastatistics.controller.param;

import lombok.Data;

@Data
public class StudentReadingRankExportRow {
    private Long studentId;
    private Long userId;

    private Long rank;              // 排名
    private String groupName;       // 班级名称
    private String studentName;     // 学生姓名

    private Double totalHours;      // 阅读总时长（小时）
    private String behavior;        // 学习行为积极性（最终填充）

    private String textbookName;    // 阅读教材名称（明细）
    private Double textbookHours;   // 阅读时长（小时）（明细）
}


