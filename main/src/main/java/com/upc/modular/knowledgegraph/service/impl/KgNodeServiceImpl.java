package com.upc.modular.knowledgegraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.knowledgegraph.entity.KgNode;
import com.upc.modular.knowledgegraph.mapper.KgNodeMapper;
import com.upc.modular.knowledgegraph.param.KgNodeSearchParam;
import com.upc.modular.knowledgegraph.service.IKgNodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.knowledgegraph.entity.KgEdge;
import com.upc.modular.knowledgegraph.service.IKgEdgeService;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.param.TextbookTree;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.modular.knowledgegraph.param.TextbookKnowledgeGraphReturnParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xth
 * @since 2025-07-17
 */
@Service
public class KgNodeServiceImpl extends ServiceImpl<KgNodeMapper, KgNode> implements IKgNodeService {

    @Resource
    private IKgEdgeService kgEdgeService;
    
    @Resource
    private ITextbookService textbookService;
    
    @Resource
    private ITextbookCatalogService textbookCatalogService;

    @Override
    public void updateKgNodeById(KgNode kgEdge) {
        if (kgEdge == null || kgEdge.getId() == null || kgEdge.getId() == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }

        this.updateById(kgEdge);
    }

    @Override
    public void deleteKgNodeById(Long id) {
        if(id == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, " ID不能为空");
        }
        this.removeById(id);
    }

    @Override
    public List<KgNode> getKgNodeByConditions(KgNodeSearchParam param) {
        if ((param.getObjectId() != null && param.getObjectId() != 0L) && (param.getNodeType() == null || "".equals(param.getNodeType()))) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "根据业务表id查询时，节点类型不能为空");
        }
        LambdaQueryWrapper<KgNode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getNodeName()), KgNode::getNodeName, param.getNodeName());
        queryWrapper.eq(StringUtils.isNotBlank(param.getNodeType()), KgNode::getNodeType, param.getNodeType());
        queryWrapper.eq(param.getObjectId() != null, KgNode::getObjectId, param.getObjectId());

        List<KgNode> kgNodeList = this.list(queryWrapper);

        return kgNodeList;
    }
    
    @Override
    @Transactional
    public boolean syncTextbookCatalogToKnowledgeGraph(Long textbookId) {
        // 1. 验证参数
        if (textbookId == null || textbookId <= 0) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        // 2. 获取教材信息
        Textbook textbook = textbookService.getById(textbookId);
        if (textbook == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "指定的教材不存在");
        }

        // 3. 检查教材是否发布
        if (textbook.getReleaseStatus() == null || textbook.getReleaseStatus() != 1 || 
            textbook.getReviewStatus() == null || textbook.getReviewStatus() != 1) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材未发布");
        }

        // 4. 获取教材的所有目录信息（使用getTextbookCatalogTree方法获取完整的目录树结构）
        List<TextbookTree> textbookTree = textbookCatalogService.getTextbookCatalogTree(textbookId);
        List<TextbookCatalog> textbookCatalogs = flattenTextbookTree(textbookTree);
        
        // 5. 查询已存在的节点（教材节点和目录节点）
        LambdaQueryWrapper<KgNode> nodeQueryWrapper = new LambdaQueryWrapper<>();
        nodeQueryWrapper.eq(KgNode::getNodeType, "TEXTBOOK").eq(KgNode::getObjectId, textbookId)
                .or()
                .eq(KgNode::getNodeType, "TEXTBOOK_CATALOG").in(KgNode::getObjectId, 
                        textbookCatalogs.stream().map(TextbookCatalog::getId).collect(Collectors.toList()));
    
        List<KgNode> existingNodes = this.list(nodeQueryWrapper);
        
        // 6. 分离已存在的节点和需要创建的节点
        Set<Long> existingTextbookCatalogIds = existingNodes.stream()
                .filter(node -> "TEXTBOOK_CATALOG".equals(node.getNodeType()))
                .map(KgNode::getObjectId)
                .collect(Collectors.toSet());
    
        KgNode textbookNode = existingNodes.stream()
                .filter(node -> "TEXTBOOK".equals(node.getNodeType()) && node.getObjectId().equals(textbookId))
                .findFirst()
                .orElse(null);
    
        // 7. 创建教材节点（如果不存在）
        if (textbookNode == null) {
            textbookNode = new KgNode();
            textbookNode.setObjectId(textbookId);
            textbookNode.setNodeType("TEXTBOOK");
            textbookNode.setNodeName(textbook.getTextbookName());
            this.save(textbookNode);
        }
    
        // 8. 创建缺失的目录节点
        List<KgNode> newCatalogNodes = new ArrayList<>();
        for (TextbookCatalog catalog : textbookCatalogs) {
            if (!existingTextbookCatalogIds.contains(catalog.getId())) {
                KgNode catalogNode = new KgNode();
                catalogNode.setObjectId(catalog.getId());
                catalogNode.setNodeType("TEXTBOOK_CATALOG");
                catalogNode.setNodeName(catalog.getCatalogName());
                newCatalogNodes.add(catalogNode);
            }
        }
    
        if (!newCatalogNodes.isEmpty()) {
            this.saveBatch(newCatalogNodes);
            existingNodes.addAll(newCatalogNodes);
        }
    
        // 9. 重新组织所有节点（包括新创建的）以便后续处理
        // 创建一个映射，从objectId到KgNode
        java.util.Map<Long, KgNode> nodeMap = existingNodes.stream()
                .collect(Collectors.toMap(KgNode::getObjectId, node -> node));
    
        // 10. 不再删除旧的关系，而是查询已存在的关系，避免重复创建
        LambdaQueryWrapper<KgEdge> edgeQueryWrapper = new LambdaQueryWrapper<>();
        edgeQueryWrapper.eq(KgEdge::getSourceNodeId, textbookNode.getId())
                .or()
                .in(KgEdge::getSourceNodeId, 
                        textbookCatalogs.stream().map(c -> nodeMap.get(c.getId()).getId()).collect(Collectors.toList()))
                .or()
                .in(KgEdge::getTargetNodeId, 
                        textbookCatalogs.stream().map(c -> nodeMap.get(c.getId()).getId()).collect(Collectors.toList()));
                        
        List<KgEdge> existingEdges = kgEdgeService.list(edgeQueryWrapper);
        
        // 创建已存在关系的集合，用于避免重复创建
        Set<String> existingEdgeKeys = existingEdges.stream()
                .map(edge -> edge.getSourceNodeId() + "->" + edge.getTargetNodeId() + ":" + edge.getRelationType())
                .collect(Collectors.toSet());

        // 11. 创建新的关系
        List<KgEdge> newEdges = new ArrayList<>();
    
        // 创建教材到章节的关系
        for (TextbookCatalog catalog : textbookCatalogs) {
            KgNode catalogNode = nodeMap.get(catalog.getId());
            if (catalogNode != null) {
                KgEdge edge = new KgEdge();
                // 章节是顶级章节（没有父章节）
                if (catalog.getFatherCatalogId() == null || catalog.getFatherCatalogId() == 0) {
                    edge.setSourceNodeId(textbookNode.getId());
                    edge.setTargetNodeId(catalogNode.getId());
                    edge.setRelationType("CONTAINS");
                    
                    // 检查关系是否已存在
                    String edgeKey = edge.getSourceNodeId() + "->" + edge.getTargetNodeId() + ":" + edge.getRelationType();
                    if (!existingEdgeKeys.contains(edgeKey)) {
                        newEdges.add(edge);
                    }
                }
            }
        }
    
        // 创建章节到子章节的关系
        for (TextbookCatalog catalog : textbookCatalogs) {
            if (catalog.getFatherCatalogId() != null && catalog.getFatherCatalogId() != 0) {
                KgNode catalogNode = nodeMap.get(catalog.getId());
                KgNode fatherCatalogNode = nodeMap.get(catalog.getFatherCatalogId());
                
                if (catalogNode != null && fatherCatalogNode != null) {
                    KgEdge edge = new KgEdge();
                    edge.setSourceNodeId(fatherCatalogNode.getId());
                    edge.setTargetNodeId(catalogNode.getId());
                    edge.setRelationType("CONTAINS");
                    
                    // 检查关系是否已存在
                    String edgeKey = edge.getSourceNodeId() + "->" + edge.getTargetNodeId() + ":" + edge.getRelationType();
                    if (!existingEdgeKeys.contains(edgeKey)) {
                        newEdges.add(edge);
                    }
                }
            }
        }
    
        // 12. 批量保存新的关系
        if (!newEdges.isEmpty()) {
            kgEdgeService.saveBatch(newEdges);
        }
    
        return true;
    }

    /**
     * 将教材目录树结构扁平化为列表
     * @param treeList 教材目录树
     * @return 扁平化的教材目录列表
     */
    private List<TextbookCatalog> flattenTextbookTree(List<TextbookTree> treeList) {
        List<TextbookCatalog> result = new ArrayList<>();
        if (treeList != null) {
            for (TextbookTree node : treeList) {
                TextbookCatalog catalog = new TextbookCatalog();
                catalog.setId(node.getCatalogId());
                catalog.setTextbookId(node.getTextbookId());
                catalog.setCatalogName(node.getCatalogName());
                catalog.setCatalogLevel(node.getCatalogLevel());
                catalog.setFatherCatalogId(node.getFatherCatalogId());
                catalog.setSort(node.getSort());
                result.add(catalog);
                
                // 递归处理子节点
                if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                    result.addAll(flattenTextbookTree(node.getChildren()));
                }
            }
        }
        return result;
    }
    
    @Override
    @Transactional // 增强：添加事务注解，保证数据操作的原子性
    public TextbookKnowledgeGraphReturnParam getTextbookKnowledgeGraph(Long textbookId) {
        // 1. 验证参数
        if (textbookId == null || textbookId <= 0) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        // 2. 获取教材信息
        Textbook textbook = textbookService.getById(textbookId);
        if (textbook == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "指定的教材不存在");
        }

        // 3. 构建返回对象
        TextbookKnowledgeGraphReturnParam result = new TextbookKnowledgeGraphReturnParam();
        result.setTextbookId(textbookId);
        result.setTextbookName(textbook.getTextbookName());

        // 4. 查询教材根节点
        LambdaQueryWrapper<KgNode> nodeQueryWrapper = new LambdaQueryWrapper<>();
        nodeQueryWrapper.eq(KgNode::getObjectId, textbookId).eq(KgNode::getNodeType, "TEXTBOOK");
        KgNode textbookNode = this.getOne(nodeQueryWrapper);

        // 如果没有教材节点，则创建一个
        if (textbookNode == null) {
            textbookNode = new KgNode();
            textbookNode.setObjectId(textbookId);
            textbookNode.setNodeType("TEXTBOOK");
            textbookNode.setNodeName(textbook.getTextbookName());
            this.save(textbookNode);
            // 重新查询以获取ID
            textbookNode = this.getOne(new LambdaQueryWrapper<KgNode>()
                .eq(KgNode::getObjectId, textbookId).eq(KgNode::getNodeType, "TEXTBOOK"));
        }

        // 5. 获取所有与教材相关的节点
        // 包括直接与教材相关的节点以及通过边连接的所有节点
        Set<KgNode> relatedNodes = new HashSet<>();
        relatedNodes.add(textbookNode);
        
        // 获取所有与教材节点直接相连的边
        List<KgEdge> directlyConnectedEdges = getRelatedEdges(textbookNode.getId());
        
        // 从这些边中提取所有节点ID
        Set<Long> relatedNodeIds = new HashSet<>();
        relatedNodeIds.add(textbookNode.getId()); // 确保包含教材节点本身
        
        for (KgEdge edge : directlyConnectedEdges) {
            relatedNodeIds.add(edge.getSourceNodeId());
            relatedNodeIds.add(edge.getTargetNodeId());
        }
        
        // 获取所有相关节点
        if (!relatedNodeIds.isEmpty()) {
            List<KgNode> nodeList = this.listByIds(relatedNodeIds);
            relatedNodes.addAll(nodeList);
            
            // 继续查找与这些节点相连的边和节点（深度查找）
            List<KgEdge> allRelatedEdges = new ArrayList<>(directlyConnectedEdges);
            Set<Long> checkedNodeIds = new HashSet<>(relatedNodeIds);
            Set<Long> newNodeIds = new HashSet<>(relatedNodeIds);
            
            // 循环查找直到没有新的节点被发现
            while (!newNodeIds.isEmpty()) {
                // 查找与新节点相关的边
                List<KgEdge> newEdges = getEdgesConnectedToNodes(newNodeIds);
                allRelatedEdges.addAll(newEdges);
                
                // 从新边中提取节点ID
                newNodeIds.clear();
                for (KgEdge edge : newEdges) {
                    // 添加源节点和目标节点（如果尚未检查过）
                    if (!checkedNodeIds.contains(edge.getSourceNodeId())) {
                        newNodeIds.add(edge.getSourceNodeId());
                        checkedNodeIds.add(edge.getSourceNodeId());
                    }
                    if (!checkedNodeIds.contains(edge.getTargetNodeId())) {
                        newNodeIds.add(edge.getTargetNodeId());
                        checkedNodeIds.add(edge.getTargetNodeId());
                    }
                }
                
                // 获取新发现的节点
                if (!newNodeIds.isEmpty()) {
                    List<KgNode> newNodes = this.listByIds(newNodeIds);
                    relatedNodes.addAll(newNodes);
                }
            }
            
            //result.setEdges(allRelatedEdges);// 在返回前去重
            result.setEdges(new ArrayList<>(new HashSet<>(allRelatedEdges)));

        }
        
        result.setNodes(new ArrayList<>(relatedNodes));

        return result;
    }

    /**
     * 获取与指定节点集合中任意节点相关联的所有关系
     * @param nodeIds 节点ID集合
     * @return 相关联的关系列表
     */
    private List<KgEdge> getEdgesConnectedToNodes(Set<Long> nodeIds) {
        if (nodeIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<KgEdge> edgeQueryWrapper = new LambdaQueryWrapper<>();
        edgeQueryWrapper.in(KgEdge::getSourceNodeId, nodeIds)
                .or()
                .in(KgEdge::getTargetNodeId, nodeIds);
        return kgEdgeService.list(edgeQueryWrapper);
    }

    /**
     * 获取与指定节点相关联的所有关系（出边和入边）
     * 这个方法逻辑清晰且独立，予以保留。
     * @param nodeId 节点ID
     * @return 相关联的关系列表
     */
    private List<KgEdge> getRelatedEdges(Long nodeId) {
        LambdaQueryWrapper<KgEdge> edgeQueryWrapper = new LambdaQueryWrapper<>();
        // 查询源节点ID或目标节点ID为指定ID的边
        edgeQueryWrapper.eq(KgEdge::getSourceNodeId, nodeId)
                .or()
                .eq(KgEdge::getTargetNodeId, nodeId);
        return kgEdgeService.list(edgeQueryWrapper);
    }
}