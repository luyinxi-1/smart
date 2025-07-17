package com.upc.modular.knowledgegraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.knowledgegraph.entity.KgEdge;
import com.upc.modular.knowledgegraph.mapper.KgEdgeMapper;
import com.upc.modular.knowledgegraph.param.KgEdgeSearchParam;
import com.upc.modular.knowledgegraph.service.IKgEdgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xth
 * @since 2025-07-17
 */
@Service
public class KgEdgeServiceImpl extends ServiceImpl<KgEdgeMapper, KgEdge> implements IKgEdgeService {

    @Override
    public void insertKgEdge(KgEdge kgEdge) {
        if (kgEdge == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        if (kgEdge.getSourceNodeId() == null || kgEdge.getSourceNodeId() == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，起始节点不能为空");
        }
        if (kgEdge.getTargetNodeId() == null || kgEdge.getTargetNodeId() == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，目标节点不能为空");
        }

        this.save(kgEdge);
    }

    @Override
    public void deleteKgEdgeById(Long id) {
        if(id == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, " ID不能为空");
        }
        this.removeById(id);
    }

    @Override
    public List<KgEdge> getKgEdgeByConditions(KgEdgeSearchParam param) {

        LambdaQueryWrapper<KgEdge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(param.getSourceNodeId() != null, KgEdge::getSourceNodeId, param.getSourceNodeId());
        queryWrapper.eq(param.getTargetNodeId() != null, KgEdge::getTargetNodeId, param.getTargetNodeId());

        List<KgEdge> kgEdgeList = this.list(queryWrapper);
        return kgEdgeList;
    }


}
