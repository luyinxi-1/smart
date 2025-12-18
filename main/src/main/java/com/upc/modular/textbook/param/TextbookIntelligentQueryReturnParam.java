package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "TextbookIntelligentQueryReturnParam", description = "教材智能搜索返回结果")
public class TextbookIntelligentQueryReturnParam {
    @ApiModelProperty("匹配到的教材名称")
    private String textbookName;

    @ApiModelProperty("匹配到的教材作者")
    private String authorName;

    @ApiModelProperty("教材更新日期")
    private LocalDateTime updateDate;

    @ApiModelProperty("匹配到的章节名称 (已去除HTML标签)")
    private String chapterName;

    @ApiModelProperty("匹配到的章节内容片段 (已去除HTML标签)")
    private String content;
    
    @ApiModelProperty("教材ID")
    private Long textbookId;
    
    @ApiModelProperty("章节ID")
    private Long chapterId;
    
    @ApiModelProperty("该教材中匹配到的章节总数")
    private Integer chapterCount;
}