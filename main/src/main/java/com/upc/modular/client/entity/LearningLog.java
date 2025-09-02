package com.upc.modular.client.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author mjh
 * @since 2025-08-26
 */
@Data
@Accessors(chain = true)
@TableName("learning_log")
@ApiModel(value = "LearningLog对象", description = "")
public class LearningLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("textbook_id")
    private Integer textbookId;

    @TableField("catalogue_id")
    private Integer catalogueId;

    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @TableField("client_uuid")
    private String clientUuid;

    @TableField(value = "operation_datetime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime operationDatetime;

    @TableField("is_delete")
    private Integer isDelete;

    @TableField("sync_status")
    private Integer syncStatus;


}
