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
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.modular.knowledgegraph.param.TextbookKnowledgeGraphReturnParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // 4. 查询或创建教材根节点
        LambdaQueryWrapper<KgNode> nodeQueryWrapper = new LambdaQueryWrapper<>();
        nodeQueryWrapper.eq(KgNode::getObjectId, textbookId).eq(KgNode::getNodeType, "TEXTBOOK");
        // 使用 one() 方法获取单个对象，更符合业务预期
        KgNode textbookNode = this.getOne(nodeQueryWrapper);

        // 如果没有教材节点，则创建一个
        if (textbookNode == null) {
            textbookNode = new KgNode();
            textbookNode.setObjectId(textbookId);
            textbookNode.setNodeType("TEXTBOOK");
            textbookNode.setNodeName(textbook.getTextbookName());
            // 优化：利用MyBatis-Plus的ID回填特性，保存后 textbookNode 对象即包含数据库生成的ID
            this.save(textbookNode);
        }

        Long textbookNodeId = textbookNode.getId();

        // 5. 【核心重构】一次性获取所有相关的“边”
        List<KgEdge> allRelatedEdges = getRelatedEdges(textbookNodeId);
        result.setEdges(allRelatedEdges); // 直接将结果设置到返回对象中

        // 6. 从已获取的“边”中提取所有关联节点的ID
        Set<Long> relatedNodeIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(allRelatedEdges)) {
            // 将所有源节点ID和目标节点ID添加到Set中，利用Set自动去重
            allRelatedEdges.forEach(edge -> {
                relatedNodeIds.add(edge.getSourceNodeId());
                relatedNodeIds.add(edge.getTargetNodeId());
            });
        }

        // 始终确保教材节点本身被包含
        relatedNodeIds.add(textbookNodeId);

        // 7. 一次性查询所有相关的“节点”
        if (!relatedNodeIds.isEmpty()) {
            List<KgNode> allNodes = this.listByIds(relatedNodeIds);
            result.setNodes(allNodes);
        } else {
            // 如果没有任何关联边，则只返回教材节点本身
            List<KgNode> nodeList = new ArrayList<>();
            nodeList.add(textbookNode);
            result.setNodes(nodeList);
        }

        return result;
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
