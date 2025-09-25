package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ApiModel(value = "教材更新申请信息参数")
public class TextbookUpdateApplicationParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("申请提交时间")
    private LocalDateTime applicationSubmitTime;

    @ApiModelProperty("提交审核原因 1新书发布 2章节更新")
    private Integer reasonForReview;

    @ApiModelProperty("提交审核描述")
    private String submitForReviewDescription;

    @ApiModelProperty("申请人姓名")
    private String applicantName;
}