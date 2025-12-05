package com.upc.modular.textbook.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("student_learning_log")
@ApiModel(value = "StudentLearningLog对象", description = "学生学习日志")
public class StudentLearningLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)   // 如果 Kingbase 这边是自增/序列，就用 AUTO
    private Long id;

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("学生ID")
    private Long studentId;

    @ApiModelProperty("学生姓名")
    @TableField("student_name")   // 数据库字段名写错了，这里显式指定
    private String studentName;

    @ApiModelProperty("用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("日志标题")
    @TableField("log_title")
    private String logTitle;

    @ApiModelProperty("学习日志内容（富文本）")
    private String content;        // longtext -> String

    @ApiModelProperty("创建时间")
    private LocalDateTime addDatetime;

    @ApiModelProperty("操作时间")
    private LocalDateTime operationDatetime;

    @ApiModelProperty("提交时间")
    private LocalDateTime submitDatetime;

    @ApiModelProperty("章节ID")
    private Long catalogId;

    @ApiModelProperty("状态 1(未提交)，2(已提交)，3(已查看)")
    private Long status;

    @ApiModelProperty("备注")
    private String remark;
}
