package com.upc.modular.knowledgegraph.service;

import com.upc.modular.knowledgegraph.entity.KgNode;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.knowledgegraph.param.KgNodeSearchParam;
import com.upc.modular.knowledgegraph.param.TextbookKnowledgeGraphReturnParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xth
 * @since 2025-07-17
 */
public interface IKgNodeService extends IService<KgNode> {

    void updateKgNodeById(KgNode kgEdge);

    void deleteKgNodeById(Long id);

    List<KgNode> getKgNodeByConditions(KgNodeSearchParam param);

    /**
     * 根据教材ID获取教材知识图谱信息
     *
     * @param textbookId 教材ID
     * @return 教材知识图谱信息
     */
    TextbookKnowledgeGraphReturnParam getTextbookKnowledgeGraph(Long textbookId);

    /**
     * 同步教材目录为知识图谱节点和关系
     *
     * @param textbookId 教材ID
     * @return 是否同步成功
     */
    boolean syncTextbookCatalogToKnowledgeGraph(Long textbookId);
}