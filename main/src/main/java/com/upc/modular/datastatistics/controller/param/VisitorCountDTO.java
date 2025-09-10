package com.upc.modular.datastatistics.controller.param;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

// 使用 Lombok 简化代码，如果不用请手动添加 Getter/Setter
import lombok.Data;
@Data
public class VisitorCountDTO {
        /**
         * 统计日期
         * @JsonFormat 用于确保返回给前端时日期格式是 "yyyy-MM-dd"
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date date;
        /**
         * 访客数量
         */
        private Long visitorCount;
}
