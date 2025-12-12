package com.upc.modular.textbook.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StudentLearningLogSaveParam对象", description = "学生学习日志保存参数")
public class StudentLearningLogSaveParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("章节ID")
    private Long catalogId;

    @ApiModelProperty("日志标题")
    private String logTitle;

    @ApiModelProperty("学习日志内容（富文本）")
    private String content;

    @ApiModelProperty("状态 1(未提交)，2(已提交)")
    private Long status;

    @ApiModelProperty("备注")
    private String remark;
}