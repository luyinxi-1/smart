package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TeachingQuestionBankPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("教学题库名称或标题")
    private String name;

    @ApiModelProperty("教学题库说明")
    @TableField("description")
    private String description;

    @ApiModelProperty("教学题库状态（0:表示已关闭，1表示已启用）")
    private Integer status;

    @ApiModelProperty("关联教材ID")
    private Long textbookId;

    @ApiModelProperty("关联的教材目录")
    private Long textbookCatalogId;

    @ApiModelProperty("备用教材目录Id（编辑章节时用）")
    private Long textbookCatalogId2;

    @ApiModelProperty("学生可作答的最大次数")
    private Integer maxAttempts;

    @ApiModelProperty("成绩取法（如0：最高分、1：平均分、2：最后一次）")
    private Integer scorePolicy;

    @ApiModelProperty("是否只查询未绑定题库(0 否 1是)")
    private Integer isUnbound = 0;
    
    @ApiModelProperty("目标目录ID列表（用于递归查询父级目录题库）")
    private List<Long> targetCatalogIds;
}