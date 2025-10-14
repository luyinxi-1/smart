package com.upc.modular.textbook.param;
import lombok.Data;
/**
 * 教材多条件组合查询请求体
 */
@Data
public class TextbookQueryReq {
    // --- 筛选条件 ---

    /**
     * 教材ID (精准匹配)
     * 对应表: textbook.id
     */
    private Long textbookId;

    /**
     * 教材名称 (模糊匹配)
     * 对应表: textbook.textbook_name
     */
    private String textbookName;

    /**
     * 教材类型ID (精准匹配)
     * 对应表: textbook.type
     */
    private Long type;

    /**
     * 章节/目录ID (精准匹配)
     * 对应表: textbook_catalog.id
     */
    private Long chapterId;

    /**
     * 教材类型名称 (模糊匹配)
     * 对应表: textbook_classification.classification_name
     */
    private String classificationName;

    // --- 分页参数 ---

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页显示条数
     */
    private Integer pageSize = 10;
}