package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
@Data
@Accessors(chain = true)
public class TeachingQuestionBankPageMidReturnParam {
    @ApiModelProperty("教学题库名称或标题")
    private String name;

    @ApiModelProperty("教学题库说明")
    private String description;

    @ApiModelProperty("教学题库状态（0:表示已关闭，1表示已启用）")
    private Integer status;

    @ApiModelProperty("关联教材ID")
    private Long textbookId;

    @ApiModelProperty("关联的教材目录")
    private Long textbookCatalogId;

    @ApiModelProperty("学生可作答的最大次数")
    private Integer maxAttempts;

    @ApiModelProperty("成绩取法（如0：最高分、1：最后一次）")
    private Integer scorePolicy;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("题目数量")
    private Long questionCount;
}
