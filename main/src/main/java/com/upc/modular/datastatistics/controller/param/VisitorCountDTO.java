package com.upc.modular.datastatistics.controller.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date; // 重要的引用变更：从 java.time.LocalDate 变为 java.util.Date

/**
 * 访客统计数据传输对象
 */
@Data
public class VisitorCountDTO {

        /**
         * 统计日期
         * 使用 java.util.Date 作为妥协方案，以兼容旧的JDBC驱动。
         * 必须使用 @JsonFormat 注解来确保返回给前端的JSON字符串是正确的日期且没有时区偏差。
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
        private Date date;

        /**
         * 访客数量
         */
        private int visitorCount;
}
