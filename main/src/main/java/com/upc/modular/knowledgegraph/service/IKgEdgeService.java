package com.upc.modular.knowledgegraph.service;

import com.upc.modular.knowledgegraph.entity.KgEdge;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.knowledgegraph.param.KgEdgeSearchParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xth
 * @since 2025-07-17
 */
public interface IKgEdgeService extends IService<KgEdge> {

    void insertKgEdge(KgEdge kgEdge);

    void deleteKgEdgeById(Long id);

    List<KgEdge> getKgEdgeByConditions(KgEdgeSearchParam param);
}
