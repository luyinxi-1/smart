package com.upc.modular.client.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.upc.modular.textbook.entity.LearningLog;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

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
public class ClientLearningLog extends LearningLog {

    @TableField("client_uuid")
    private String clientUuid;

    @TableField("is_delete")
    private Integer isDelete;

    @TableField("sync_status")
    private Integer syncStatus;


}
