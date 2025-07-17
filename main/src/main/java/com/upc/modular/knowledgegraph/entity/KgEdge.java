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
@TableName("kg_edge")
@ApiModel(value = "KgEdge对象", description = "")
public class KgEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("知识图谱关系ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("起始节点ID")
    @TableField("source_node_id")
    private Long sourceNodeId;

    @ApiModelProperty("目标节点ID")
    @TableField("target_node_id")
    private Long targetNodeId;

    @ApiModelProperty("关系类型: CONTAINS, DEPENDS_ON, TESTS, HAS_MATERIAL 等")
    @TableField("relation_type")
    private String relationType;

    @ApiModelProperty("关系信息")
    @TableField("properties")
    private String properties;

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
