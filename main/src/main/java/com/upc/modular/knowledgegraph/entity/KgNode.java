package com.upc.modular.knowledgegraph.entity;

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
 * @author xth
 * @since 2025-07-17
 */
@Data
@Accessors(chain = true)
@TableName("kg_node")
@ApiModel(value = "KgNode对象", description = "")
public class KgNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("知识图谱节点ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("节点类型: TEXTBOOK, TEXTBOOK_CATALOG, KNOWLEDGE_POINT, QUESTION_BANK, MATERIAL")
    @TableField("node_type")
    private String nodeType;

    @ApiModelProperty("节点名称，如章节名、知识点名")
    @TableField("node_name")
    private String nodeName;

    @ApiModelProperty("节点的详细属性，以富文本格式存储")
    @TableField("properties")
    private String properties;

    @ApiModelProperty("对应原始业务表的ID，如textbook_catalog.id")
    @TableField("object_id")
    private Long objectId;

    @ApiModelProperty("思政创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creator;

    @ApiModelProperty("思政材料创建时间")
    @TableField(value = "add_datetime", fill = FieldFill.INSERT)
    private LocalDateTime addDatetime;

    @ApiModelProperty("思政操作人")
    @TableField(value = "operator", fill = FieldFill.UPDATE)
    private Long operator;

    @ApiModelProperty("思政材料操作时间")
    @TableField(value = "operation_datetime", fill = FieldFill.UPDATE)
    private LocalDateTime operationDatetime;


}
