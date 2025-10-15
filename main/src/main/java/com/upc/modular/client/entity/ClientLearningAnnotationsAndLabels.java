package com.upc.modular.client.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习批注和标注 客户端数据传输对象
 */
@Data
public class ClientLearningAnnotationsAndLabels {

    private Long id; // 客户端本地的ID
    private String content;
    private Long textbookId;
    private Long catalogId;
    private Long creator;
    private LocalDateTime addDatetime;
    private Long operator;
    private LocalDateTime operationDatetime;
    private String clientUuid;
    private Integer isDelete;
    private Integer syncStatus; // 客户端的同步状态
    private String positionInfo;

}