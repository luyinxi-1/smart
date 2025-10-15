package com.upc.modular.client.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习笔记 客户端数据传输对象
 */
@Data
public class ClientLearningNotes {

    private Long id; // 客户端本地ID
    private String content;
    private Long textbookId;
    private Long catalogueId;
    private Long creator;
    private LocalDateTime addDatetime;
    private Long operator;
    private LocalDateTime operationDatetime;
    private String clientUuid;
    private String noteName;
    private Integer isDelete;
    private Integer syncStatus;
    private String positionInfo;
}