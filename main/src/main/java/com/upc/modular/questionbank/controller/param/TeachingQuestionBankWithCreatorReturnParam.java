package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ApiModel(value = "TeachingQuestionBankWithCreatorReturnParam对象", description = "包含创建人姓名的题库信息")
public class TeachingQuestionBankWithCreatorReturnParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

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

    @ApiModelProperty("成绩取法（如0：最高分、1：平均分、2：最后一次）")
    private Integer scorePolicy;

    @ApiModelProperty("创建人ID")
    private Long creator;

    @ApiModelProperty("创建人姓名")
    private String creatorName;

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作人")
    private Long operator;

    @ApiModelProperty("操作时间")
    private LocalDateTime operationDatetime;
}
