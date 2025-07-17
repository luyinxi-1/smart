package com.upc.modular.knowledgegraph.service;

import com.upc.modular.knowledgegraph.entity.KgNode;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.knowledgegraph.param.KgNodeSearchParam;

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

    void updatekgNodeById(KgNode kgEdge);

    void deletekgEdgeById(Long id);

    List<KgNode> getkgNodeByConditions(KgNodeSearchParam param);
}
