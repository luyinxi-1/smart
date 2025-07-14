package com.upc.modular.textbook.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
@Data
@Accessors(chain = true)
@TableName("learning_log")
@ApiModel(value = "LearningLog对象", description = "")
public class LearningLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("教材id")
    @TableField("textbook_id")
    private Long textbookId;

    @ApiModelProperty("目录章节id")
    @TableField("catalogue_id")
    private Long catalogueId;

    @ApiModelProperty("学习时间戳")
    @TableField("study_datetime")
    private LocalDateTime studyDatetime;

    @ApiModelProperty("用户id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("添加时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;


}
