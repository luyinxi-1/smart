package com.upc.modular.knowledgegraph.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/17 19:48
 */
@Data
public class KgEdgeSearchParam {

    @ApiModelProperty("起始节点ID")
    @TableField("source_node_id")
    private Long sourceNodeId;

    @ApiModelProperty("目标节点ID")
    @TableField("target_node_id")
    private Long targetNodeId;
}
