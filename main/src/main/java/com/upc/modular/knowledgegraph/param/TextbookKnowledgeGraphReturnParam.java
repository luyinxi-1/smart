package com.upc.modular.knowledgegraph.param;

import com.upc.modular.knowledgegraph.entity.KgNode;
import com.upc.modular.knowledgegraph.entity.KgEdge;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "TextbookKnowledgeGraphReturnParam", description = "教材知识图谱返回参数")
public class TextbookKnowledgeGraphReturnParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("知识节点列表")
    private List<KgNode> nodes;

    @ApiModelProperty("节点关系列表")
    private List<KgEdge> edges;
}