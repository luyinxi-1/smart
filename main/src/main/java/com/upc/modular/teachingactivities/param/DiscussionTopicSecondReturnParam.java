package com.upc.modular.teachingactivities.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiscussionTopicSecondReturnParam {
    @ApiModelProperty("教学活动主键")
    private Long id;

    @ApiModelProperty("话题的标题")
    private String topicTitle;

    @ApiModelProperty("话题的内容")
    private String topicContent;

    @ApiModelProperty("话题的类型")
    private String type;

    @ApiModelProperty("留言的类型")
    private Integer messageType;

    @ApiModelProperty("讨论关联的教材主表ID")
    private Long textbookId;

    @ApiModelProperty("讨论关联的教材目录ID")
    private Long textbookCatalogId;

    @ApiModelProperty("话题创建人ID")
    private Long creator;

    @ApiModelProperty("话题创建时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("话题操作人ID")
    private Long operator;

    @ApiModelProperty("话题操作时间")
    private LocalDateTime operationDatetime;

    @ApiModelProperty("讨论关联的教材名称")
    private String textbookName;

    @ApiModelProperty("讨论关联的教材目录名称")
    private String textbookCatalogName;

    @ApiModelProperty("创建人姓名")
    private String nickName;
}
