package com.upc.modular.datastatistics.controller.param;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyTrendDTO {

    /**
     * 日期标签 (例如: "2022-01-01", "2022-01", "2022-W05")
     */
    private String dateLabel;

    /**
     * 该日期单位下的总学习时长（秒）
     */
    private Long duration;
}