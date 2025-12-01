package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TeachingQuestionBankPageReturnParam {
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

    @ApiModelProperty("关联教材名称")
    private String textbookName;

    @ApiModelProperty("关联的教材目录")
    private Long textbookCatalogId;

    @ApiModelProperty("备用教材目录Id")
    private Long textbookCatalogId2;

    @ApiModelProperty("学生可作答的最大次数")
    private Integer maxAttempts;

    @ApiModelProperty("成绩取法（如0：最高分、1：平均分、2：最后一次）")
    private Integer scorePolicy;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("题目数量")
    private Long questionCount;

    @ApiModelProperty("创建人姓名")
    private String creatorName;

    @ApiModelProperty("是否为当前用户创建")
    private Boolean isCreatedByCurrentUser;
    
    @ApiModelProperty("教材发布状态")
    private String releaseStatus;
}