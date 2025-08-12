package com.upc.modular.textbook.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import com.upc.modular.textbook.entity.Textbook;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TextbookPageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("作者姓名")
    private String authorName;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("教材发布状态（1 已发布/ 0 未发布）")
    private Integer releaseStatus;

    @ApiModelProperty("教材审核状态（0 未提交审核；1 审核通过）")
    private Integer reviewStatus;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("教材分类id")
    private Long classificationId;

}
