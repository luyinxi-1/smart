package com.upc.modular.teachingactivities.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/7 19:49
 */
@Data
public class DiscussionTopicSearchParam {

    @ApiModelProperty("话题的标题")
    private String topicTitle;

    @ApiModelProperty("话题的类型")
    private Integer type;

    @ApiModelProperty("讨论关联的教材主表")
    private Long textbookId;

    @ApiModelProperty("讨论关联的教材目录")
    private Long textbookCatalogId;

    @ApiModelProperty("留言的类型")
    private Integer messageType;

    @ApiModelProperty("发起人类型")
    private Integer identityType;

    /**
     * 排序方式
     * 0: 按创建日期排序 (默认)
     * 1: 按最新一条回复时间排序
     * 2: 按回复数排序
     */
    @ApiModelProperty("排序方式：0: 按创建日期排序，1: 按最新一条回复时间排序2: 按回复数排序")
    private Integer sortType;

    @ApiModelProperty("当前页码")
    private Long current = 1L;

    @ApiModelProperty("每页条数")
    private Long size = 10L;

    @ApiModelProperty("教材名称")
    private String textbookName;
}