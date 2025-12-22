package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "TextbookIntelligentQueryGroupedReturnParam", description = "教材智能搜索返回结果（按书籍分组）")
public class TextbookIntelligentQueryGroupedReturnParam {
    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("教材作者")
    private String authorName;

    @ApiModelProperty("教材更新日期")
    private LocalDateTime updateDate;

    @ApiModelProperty("该教材中匹配到的章节总数")
    private Integer chapterCount;

    @ApiModelProperty("匹配到的章节列表")
    private List<ChapterInfo> chapters;

    @Data
    @ApiModel(value = "ChapterInfo", description = "章节信息")
    public static class ChapterInfo {
        @ApiModelProperty("章节ID")
        private Long chapterId;

        @ApiModelProperty("章节名称 (已去除HTML标签)")
        private String chapterName;

        @ApiModelProperty("章节内容片段 (已去除HTML标签)")
        private String content;
    }
}