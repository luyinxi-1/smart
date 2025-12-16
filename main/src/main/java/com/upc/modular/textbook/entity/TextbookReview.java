package com.upc.modular.textbook.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fwx
 * @since 2025-08-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("textbook_review")
@ApiModel(value = "TextbookReview对象", description = "")
public class TextbookReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("审核结果描述")
    @TableField(value ="description_of_audit_results")
    private String descriptionOfAuditResults;

    @ApiModelProperty("创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("审核结果 0未通过 1通过 NULL待审核")
    @TableField(value ="audit_result")
    private Integer auditResult; // 移除 "= 0" 的默认值

    @ApiModelProperty("创建者")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("提交审核原因 1新书发布 2 章节更新  3取消发布")
    @TableField(value ="reason_for_review")
    private Integer reasonForReview;

    @ApiModelProperty("提交审核描述")
    @TableField(value ="submit_for_review_description")
    private String submitForReviewDescription;

    @ApiModelProperty("教材创建人")
    @TableField(value ="textbook_creator")
    private Long textbookCreator;

    @ApiModelProperty("书籍id")
    @TableField(value ="textbook_id")
    private Long textbookId;

    @ApiModelProperty("教材版本号")
    @TableField(value = "textbook_version_number")
    private String textbookVersionNumber;

    @ApiModelProperty("主键 教材审核表的唯一id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


}