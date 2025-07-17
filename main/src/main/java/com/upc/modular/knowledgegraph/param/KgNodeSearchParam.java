package com.upc.modular.knowledgegraph.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: xth
 * @Date: 2025/7/17 10:29
 */
@Data
public class KgNodeSearchParam {
    @ApiModelProperty("节点类型: TEXTBOOK, TEXTBOOK_CATALOG, KNOWLEDGE_POINT, QUESTION_BANK, MATERIAL")
    @TableField("node_type")
    private String nodeType;

    @ApiModelProperty("节点名称，如章节名、知识点名")
    @TableField("node_name")
    private String nodeName;

    @ApiModelProperty("对应原始业务表的ID，如textbook_catalog.id")
    @TableField("object_id")
    private Long objectId;
}
