package com.upc.modular.datastatistics.controller.param; // 确保包名正确

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStudyDurationDto {

    /**
     * 统计日期
     * 使用 @JsonFormat 注解，确保返回给前端的格式是 'yyyy-MM-dd'
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8") // <-- 参照范例添加
    private Date date; // <-- 参照范例，使用 java.util.Date
    /**
     * 学习时长（单位：秒）
     * 这个字段直接映射 MyBatis 从数据库查询出的结果。
     */
    @JsonIgnore
   private Long durationInSeconds;
    /**
     * 学习时长（分钟）
     */
    private Long durationInMinutes;
}