package com.upc.modular.datastatistics.controller.param;

import com.fasterxml.jackson.annotation.JsonFormat; // 关键：导入这个注解
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorCountDTO {

        /**
         * 统计日期
         * 使用 @JsonFormat 注解来指定输出到前端的格式
         */
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8") // <-- 添加这一行
        private Date date;

        /**
         * 访客数量
         */
        private Long visitorCount;
}