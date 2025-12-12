package com.upc.modular.textbook.param;

import lombok.Data;

@Data
public class TextbookRecordPageParam {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    private Long textbookId;     // 可选：按教材过滤
    private Long catalogId;      // 可选：按章节过滤
    private Integer status;      // 1新增、2修改、3删除
    private String keyword;      // 模糊查询章节名或教材名
}
