package com.upc.modular.textbook.param;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TextbookRecordPageDto {

    private Long id;

    private Long textbookId;
    private String textbookName;

    private Long catalogId;
    private String catalogName;

    private Long creator;
    private String creatorName; // 可选：如果你需要展示创建人名称

    private LocalDateTime addDatetime;

    private Integer status; // 1新增，2修改，3删除
}

