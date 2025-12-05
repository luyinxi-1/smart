package com.upc.modular.textbook.param;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "StudentLearningLogPageSearchParam对象", description = "学生学习日志分页查询参数")
public class StudentLearningLogPageSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("章节ID")
    private Long catalogId;

    @ApiModelProperty("学生ID")
    private Long studentId;

    @ApiModelProperty("状态 1(未提交)，2(已提交)，3(已查看)")
    private Long status;

    private String title;// 日志标题 (通用)
    private String studentName; // 学生姓名 (仅管理员/老师用)
}